package ca.unbc.meng.face;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.TreeSet;

import ca.unbc.meng.face.thread.AnalyzeThread;
import ca.unbc.meng.face.thread.DataStore;

public class MutlithreadRecognize {
	public static void main(String[] args) throws Exception {
		new MutlithreadRecognize().process(new DataStore(new Paras()));
	}

	public int process(DataStore dataStore) throws IOException, InterruptedException {
		final AnalyzeThread[] tAry = new AnalyzeThread[dataStore.paras.threadNum];
		for (int i = 0; i < tAry.length; i++)
			tAry[i] = new AnalyzeThread(dataStore);
		int cnt = 0;
		for (int i : dataStore.getTestIdx()) {
			int tid = cnt % tAry.length;
			cnt++;
			tAry[tid].imgIdList.add(i);
		}
		for (int i = 0; i < tAry.length; i++)
			tAry[i].start();

		Thread tt = new Thread() {
			public void run() {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				boolean alive = true;
				while (alive) {
					System.out.println(sdf.format(cal.getTime()));
					alive = false;
					for (int i = 0; i < tAry.length; i++) {
						double p = 100.0 * tAry[i].totalCnt
								/ tAry[i].imgIdList.size();
						System.out.println("Thread " + i + ":"
								+ tAry[i].totalCnt + "-" + p + "%");
						alive = alive || tAry[i].isAlive();
					}
					// System.out.println("Store size: " + dataStore.getSize());
					printRate(tAry);
					System.out.println();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		tt.start();
		tt.join();
		printRate(tAry);

		Collection<Integer> incorrectIdList = new TreeSet<>();
		for (int i = 0; i < tAry.length; i++) {
			incorrectIdList.addAll(tAry[i].incorrectIdList);
		}
		System.out.println(incorrectIdList);
		return incorrectIdList.size();
	}

	private void printRate(AnalyzeThread[] tAry) {
		double cnt = 0;
		double total = 0;
		for (int i = 0; i < tAry.length; i++) {
			cnt += tAry[i].correctCnt;
			total += tAry[i].totalCnt;
		}
		double p = total == 0 ? 0 : cnt * 100.0 / total;
		System.out.println("rate:" + p + "%");
	}
}
