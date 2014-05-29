package ca.unbc.meng.face;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;

import ca.unbc.meng.face.thread.DataStore;

public class CompareRun {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		Paras paras = new Paras();
		DataStore ds = new DataStore(paras);
		MutlithreadRecognize mr = new MutlithreadRecognize();
		LinkedList<int[]> list = new LinkedList<>();
		int distTypeCnt = 3;
		for (int i = 11; i <= 15; i++) {
			paras.test = i;
			int[] r = new int[distTypeCnt + 1];
			r[0] = i;
			
			paras.normrType = 0;
			paras.distType = 0;
			ds.reloadNewTest();
			r[1] = mr.process(ds);
			
			paras.normrType = 1;
			paras.distType = 1;
			ds.reloadNewTest();
			r[2] = mr.process(ds);
			
			paras.normrType = 1;
			paras.distType = 2;
			ds.reloadNewTest();
			r[3] = mr.process(ds);
			
			System.out.println(Arrays.toString(r));
			list.add(r);
		}
		System.out.println("finish:");

		String fn = "result\\" + System.currentTimeMillis() + ".txt";
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fn))) {
			for (int[] r : list) {
				System.out.println(Arrays.toString(r));
				bw.append(Arrays.toString(r)).append("\n");
			}
			bw.append("train").append(String.valueOf(paras.train)).append("\n");
			bw.append("pixcel").append(String.valueOf(paras.PIXCEL_SIZE))
					.append("\n");
			bw.append("shift").append(String.valueOf(paras.SHIFT)).append("\n");
		}
		
		TreeSet<String> rankedList = new TreeSet<>(new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				// TODO Auto-generated method stub
				return 0;
			}
		});
	}

}
