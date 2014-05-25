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
					double[] ntestm = normr(testm);
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
				double[] ntrainm = normr(trainm);
				double dist;
				switch (dataStrore.paras.distType) {
				case 0:
					dist = MathUtils.dist0(ntrainm, ntestm);
				case 1:
					dist = MathUtils.dist1(ntrainm, ntestm);
				case 2:
					dist = MathUtils.dist2(ntrainm, ntestm);
				default:
					dist = MathUtils.dist0(ntrainm, ntestm);
				}
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

	private double[] normr(double[] trainm) {
		switch (dataStrore.paras.normrType) {
		case 0:
			return MathUtils.normalize(trainm);
		case 1:
			return MathUtils.normalize1(trainm);
		default:
			return MathUtils.normalize(trainm);
		}
	}
}
