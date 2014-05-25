package ca.unbc.meng.face;

public class MathUtils {
	public static double[] normalize(double[] ipt) {
		double sum = 0;
		for (double d : ipt)
			sum += Math.pow(d, 2);
		sum = Math.sqrt(sum);
		double[] ret = new double[ipt.length];
		for (int i = 0; i < ipt.length; i++) {
			ret[i] = ipt[i] / sum;
		}
		return ret;
	}

	public static double[] normalize1(double[] ipt) {
		double[] ret = new double[ipt.length];
		for (int i = 0; i < ipt.length; i++) {
			ret[i] = ipt[i] / ipt[4];
		}
		return ret;
	}

	public static double dist0(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += Math.pow(a[i] - b[i], 2);
		}
		return sum;
	}

	public static double dist1(double[] a, double[] b) {
		double sum = 0;
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] += Math.pow(a[i] - b[i], 2);
		}
		c = filter(c);
		for (int i = 0; i < a.length; i++) {
			sum += c[i];
		}
		return Math.abs(sum);
	}

	private static double[] Gaussian3x3 = { 1, 2, 1, 2, 4, 2, 1, 2, 1, };

	private static double[] filter(double[] v) {
		double[] ret = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			ret[i] = Gaussian3x3[i] * v[i];
		}
		return ret;
	}

	public static double dist2(double[] a, double[] b) {
		return dist0(ssum(a), ssum(b));
	}

	private static double[] ssum(double[] v) {
		double[] ret = new double[3];
		ret[0] = v[4] * 2;
		ret[1] = (v[2] + v[4] + v[6] + v[8]) / 4.0 * Math.sqrt(2);
		ret[2] = (v[1] + v[3] + v[5] + v[7]) / 4.0;
		return ret;
	}
}
