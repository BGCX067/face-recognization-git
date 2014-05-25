package ca.unbc.meng.face;

import java.util.LinkedList;
import java.util.List;

public class Paras {
	public int PIXCEL_SIZE = 3;
	public int SHIFT = 4;
	public List<Double> angle = new LinkedList<>();
	public int train = 2;
	public int test = 10;
	public int threadNum = Runtime.getRuntime().availableProcessors();

	public int normrType = 1;
	public int distType = 1;

	public Paras() {
//		angle.add(0.05);
	}

}
