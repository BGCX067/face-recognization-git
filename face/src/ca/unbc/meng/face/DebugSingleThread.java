package ca.unbc.meng.face;

import ca.unbc.meng.face.thread.AnalyzeThread;
import ca.unbc.meng.face.thread.DataStore;

public class DebugSingleThread {
	public static void main(String[] args) throws Exception {
		DataStore dataStore = new DataStore(new Paras());
		AnalyzeThread t = new AnalyzeThread(dataStore);
		for (int i : dataStore.getTestIdx()) {
			t.imgIdList.add(i);
		}
		t.run();

		double p = t.correctCnt * 100.0 / t.totalCnt;
		System.out.println("rate:" + p + "%");
	}
}
