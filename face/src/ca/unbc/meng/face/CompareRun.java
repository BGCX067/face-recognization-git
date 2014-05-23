package ca.unbc.meng.face;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import ca.unbc.meng.face.thread.DataStore;

public class CompareRun {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		Paras paras = new Paras();
		DataStore ds = new DataStore(paras);
		MutlithreadRecognize mr = new MutlithreadRecognize();
		LinkedList<int[]> list = new LinkedList<>();
		for (int i = 15; i <= 17; i++) {
			paras.test = i;
			paras.distType = 0;
			ds.reloadNewTest();
			int[] r = new int[2];
			r[0] = mr.process(ds);
			paras.distType = 1;
			r[1] = mr.process(ds);
			System.out.println(Arrays.toString(r));
		}

		for (int[] r : list)
			System.out.println(Arrays.toString(r));
	}

}
