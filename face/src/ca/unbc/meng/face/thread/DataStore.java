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
	
	public void reloadNewTest() throws FileNotFoundException, IOException{
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
				if (!paras.rotate)
					continue;
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
				paras.PIXCEL_SIZE + 2);
		for (double a : paras.angle) {
			ret.add(rotate(origPixcel, a));
			ret.add(rotate(origPixcel, -a));
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
		int nrows = paras.PIXCEL_SIZE + 2;
		double[] out_img = new double[paras.PIXCEL_SIZE * paras.PIXCEL_SIZE];
		double center = (nrows - 1) / 2.0;
		double cs = Math.cos(angle);
		double sn = Math.sin(angle);

		for (int i = 1; i < nrows - 1; i++) {
			for (int j = 1; j < nrows - 1; j++) {
				double x = (i - center) * cs - (j - center) * sn + center;
				double y = (i - center) * sn + (j - center) * cs + center;
				int pos = (i - 1) * paras.PIXCEL_SIZE + j - 1;

				// Outside the original image
				if (x < 0 || x >= nrows - 1) {
					out_img[pos] = 0;
					continue;
				}
				if (y < 0 || y >= nrows - 1) {
					out_img[pos] = 0;
					continue;
				}

				// Bilinear interpolation
				int x0 = (int) Math.floor(x);
				int y0 = (int) Math.floor(y);
				int x1 = x0 + 1;
				int y1 = y0 + 1;

				double p0 = in_img[x0 * nrows + y0];
				double p1 = in_img[x1 * nrows + y0];
				double p = (x1 - x) * p0 + (x - x0) * p1;

				double p2 = in_img[x0 * nrows + y1];
				double p3 = in_img[x1 * nrows + y1];
				double q = (x1 - x) * p2 + (x - x0) * p3;
				out_img[pos] = (y1 - y) * p + (y - y0) * q;
			}
		}
		return out_img;
	}
}
