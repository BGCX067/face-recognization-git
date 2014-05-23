package ca.unbc.meng.face.thread;

import java.util.LinkedList;
import java.util.List;

import ca.unbc.meng.face.MathUtils;

public class AnalyzeThread extends Thread {
	private DataStore dataStrore;
	public List<Integer> imgIdList = new LinkedList<>();
	public List<Integer> incorrectIdList = new LinkedList<>();

	public double correctCnt = 0;
	public int totalCnt = 0;

	public AnalyzeThread(DataStore dataStrore) {
		this.dataStrore = dataStrore;
	}

	public void run() {
		for (Integer idTest : imgIdList) {
			int[] totalvotes = new int[16];
			int loopSize = dataStrore.calLoopSize();
			for (int x = 0; x < loopSize; x++) {
				for (int y = 0; y < loopSize; y++) {
					double[] testm = dataStrore.getTestPixcel(idTest, x, y);
					double[] ntestm = MathUtils.normalize(testm);
					List<Integer> ids = findMinDistImg(x, y, ntestm);
					for (Integer id : ids) {
						int label = dataStrore.getLabel(id);
						// System.out.println(ids + "/" + label);
						totalvotes[label]++;
					}
				}
			}
			List<Integer> result = countVote(totalvotes);
			int correctLabel = dataStrore.getLabel(idTest);
			if (result.contains(correctLabel)) {
				correctCnt += 1.0 / result.size();
			} else {
				incorrectIdList.add(idTest);
			}
			totalCnt++;
		}
	}

	private List<Integer> countVote(int[] totalvotes) {
		int max = 0;
		LinkedList<Integer> ret = new LinkedList<>();
		for (int i = 0; i < totalvotes.length; i++) {
			if (totalvotes[i] > max) {
				ret.clear();
				ret.add(i);
				max = totalvotes[i];
			} else if (totalvotes[i] == max) {
				ret.add(i);
			}
		}
		return ret;
	}

	private List<Integer> findMinDistImg(int x, int y, double[] ntestm) {
		double minDist = Double.MAX_VALUE;
		LinkedList<Integer> ret = new LinkedList<>();
		for (int idTrain : dataStrore.getTrainIdx()) {
			for (double[] trainm : dataStrore.getTrainPixcels(idTrain, x, y)) {
				double[] ntrainm = MathUtils.normalize(trainm);
				double dist;
				if (dataStrore.paras.distType == 0)
					dist = MathUtils.dist(ntrainm, ntestm);
				else
					dist = MathUtils.dist2(ntrainm, ntestm);
				if (minDist == dist) {
					ret.add(idTrain);
				} else if (dist < minDist) {
					minDist = dist;
					ret.clear();
					ret.add(idTrain);
				}
			}
		}
		return ret;
	}
}
