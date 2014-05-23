package ca.unbc.meng.face;

import java.util.LinkedList;
import java.util.List;

public class Paras {
	public int PIXCEL_SIZE = 3;
	public int SHIFT = 4;
	public List<Double> angle = new LinkedList<>();
	public boolean rotate = false;
	public int train = 2;
	public int test = 10;
	public int threadNum = 8;
	
	public int distType = 0;

	public Paras() {
		angle.add(Math.PI / 60);
		angle.add(Math.PI / 30);
	}
	
	
}
