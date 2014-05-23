package ca.unbc.meng.face;

import ca.unbc.meng.face.thread.AnalyzeThread;
import ca.unbc.meng.face.thread.DataStore;

public class SingleCompare {
	public static void main(String[] args) throws Exception {
		Paras paras = new Paras();
//		paras.rotate = true;
//		paras.SHIFT = 3;
		
		DataStore dataStore = new DataStore(paras);
		AnalyzeThread t = new AnalyzeThread(dataStore);
		t.imgIdList.add(6);
		t.run();

		double p = t.correctCnt * 100.0 / t.totalCnt;
		System.out.println("rate:" + p + "%");
	}
}
