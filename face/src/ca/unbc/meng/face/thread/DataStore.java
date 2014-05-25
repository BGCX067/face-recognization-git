package ca.unbc.meng.face.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.unbc.meng.face.Paras;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class DataStore {
	private Map<String, List<double[]>> rotateStore = new ConcurrentHashMap<>();
	private Map<String, List<double[]>> pixcelStore = new ConcurrentHashMap<>();

	private int[] trainIdx;
	private int[][] imgData;
	private int[] testIdx;
	private int[] imgLabel;
	public final Paras paras;
	private final int IMG_WIDTH;

	public DataStore(Paras paras) throws IOException {
		this.paras = paras;
		loadImgData();
		loadImgLabel();
		loadIdx();
		IMG_WIDTH = (int) Math.sqrt(imgData[0].length);
	}

	public void reloadNewTest() throws FileNotFoundException, IOException {
		loadIdx();
	}

	private void loadImgData() throws FileNotFoundException, IOException {
		LinkedList<int[]> list = new LinkedList<>();
		try (BufferedReader is = new BufferedReader(new FileReader(
				"data/Yale32.csv"))) {
			String line = is.readLine();
			while (line != null) {
				String[] t = line.split(",");
				list.add(processLine(t));
				line = is.readLine();
			}
		}
		imgData = list.toArray(new int[0][]);
	}

	private static int[] processLine(String[] strings) {
		int[] intarray = new int[strings.length];
		int i = 0;
		for (String str : strings) {
			intarray[i] = Integer.parseInt(str);
			i++;
		}
		return intarray;
	}

	private void loadImgLabel() throws FileNotFoundException, IOException {
		MatFileReader mfr = new MatFileReader();
		Map<String, MLArray> content = mfr
				.read(new File("data/Yale_32x32.mat"));
		double[][] gnd = ((MLDouble) content.get("gnd")).getArray();
		imgLabel = new int[gnd.length];
		for (int i = 0; i < gnd.length; i++)
			imgLabel[i] = (int) gnd[i][0];
	}

	private void loadIdx() throws FileNotFoundException, IOException {
		MatFileReader mfr = new MatFileReader();
		String fn = "data/" + paras.train + "Train/" + paras.test + ".mat";
		Map<String, MLArray> content = mfr.read(new File(fn));
		trainIdx = toIndexArray(((MLDouble) content.get("trainIdx")).getArray());
		testIdx = toIndexArray(((MLDouble) content.get("testIdx")).getArray());
	}

	private static int[] toIndexArray(double[][] t) {
		int[] ret = new int[t.length];
		for (int i = 0; i < t.length; i++)
			ret[i] = (int) t[i][0] - 1;
		Arrays.sort(ret);
		return ret;
	}

	public int[] getTestIdx() {
		return testIdx;
	}

	public int[] getTrainIdx() {
		return trainIdx;
	}

	public int[] getImage(int id) {
		return imgData[id];
	}

	public int getLabel(int id) {
		return imgLabel[id];
	}

	public int getSize() {
		return pixcelStore.size();
	}

	public double[] getTestPixcel(int id, int x, int y) {
		return getMacropixcel(imgData[id], x, y, paras.PIXCEL_SIZE);
	}

	public List<double[]> getTrainPixcels(int idTrain, int x, int y) {
		String key = idTrain + "," + x + "," + y;
		if (pixcelStore.containsKey(key))
			return pixcelStore.get(key);
		List<double[]> ret = buildTrainPixcels(idTrain, x, y);
		pixcelStore.put(key, ret);
		return ret;
	}

	public int calLoopSize() {
		return IMG_WIDTH - paras.PIXCEL_SIZE;
	}

	private List<double[]> buildTrainPixcels(int idTrain, int x, int y) {
		LinkedList<double[]> ret = new LinkedList<>();
		for (int shiftx = -paras.SHIFT; shiftx <= paras.SHIFT; shiftx++) {
			for (int shifty = -paras.SHIFT; shifty <= paras.SHIFT; shifty++) {
				int nx = x + shiftx;
				int ny = y + shifty;
				// un-rotate
				double[] ur = getMacropixcel(imgData[idTrain], nx, ny,
						paras.PIXCEL_SIZE);
				if (ur == null)
					continue;
				else
					ret.add(ur);
				// rotate
				String key = idTrain + "," + nx + "," + ny;
				if (rotateStore.containsKey(key))
					ret.addAll(rotateStore.get(key));
				else {
					List<double[]> list = buildRotatePixcels(imgData[idTrain],
							nx, ny);
					rotateStore.put(key, list);
					ret.addAll(list);
				}
			}
		}
		return ret;
	}

	private List<double[]> buildRotatePixcels(int[] in_img, int x, int y) {
		LinkedList<double[]> ret = new LinkedList<>();
		if (x - 1 < 0 || x + paras.PIXCEL_SIZE + 2 >= IMG_WIDTH)
			return ret;
		if (y - 1 < 0 || y + paras.PIXCEL_SIZE + 2 >= IMG_WIDTH)
			return ret;
		double[] origPixcel = getMacropixcel(in_img, x - 1, y - 1,
				paras.PIXCEL_SIZE);
		for (double a : paras.angle) {
			ret.add(rotate(origPixcel, a));
			ret.add(rotateRevise(origPixcel, -a));
		}
		return ret;
	}

	private double[] getMacropixcel(int[] in_img, int x, int y, int pixcelSize) {
		if (x < 0 || x + pixcelSize >= IMG_WIDTH)
			return null;
		if (y < 0 || y + pixcelSize >= IMG_WIDTH)
			return null;
		double[] ret = new double[pixcelSize * pixcelSize];
		for (int colid = 0; colid < pixcelSize; colid++)
			for (int rowid = 0; rowid < pixcelSize; rowid++) {
				int mi = (x + colid) * IMG_WIDTH + y + rowid;
				ret[colid * pixcelSize + rowid] = in_img[mi];
			}
		return ret;
	}

	private double[] rotate(double in_img[], double angle) {
		double[] out_img = new double[paras.PIXCEL_SIZE * paras.PIXCEL_SIZE];
		out_img[4] = in_img[4];
		int[] idx1 = { 1, 3, 5, 7 };
		int[] idx2 = { 0, 2, 6, 8 };
		for (int i = 0; i <= idx1.length; i++) {
			out_img[i] = in_img[i] * (1 - angle)
					+ in_img[(i + 1) % idx1.length] * angle;
		}
		for (int i = 0; i <= idx2.length; i++) {
			out_img[i] = in_img[i] * (1 - angle)
					+ in_img[(i + 1) % idx2.length] * angle;
		}
		return out_img;
	}

	private double[] rotateRevise(double in_img[], double angle) {
		double[] out_img = new double[paras.PIXCEL_SIZE * paras.PIXCEL_SIZE];
		out_img[4] = in_img[4];
		int[] idx1 = { 1, 3, 5, 7 };
		int[] idx2 = { 0, 2, 6, 8 };
		for (int i = 0; i <= idx1.length; i++) {
			out_img[i] = in_img[i] * (1 - angle)
					+ in_img[(i + 3) % idx1.length] * angle;
		}
		for (int i = 0; i <= idx2.length; i++) {
			out_img[i] = in_img[i] * (1 - angle)
					+ in_img[(i + 3) % idx2.length] * angle;
		}
		return out_img;
	}
}
