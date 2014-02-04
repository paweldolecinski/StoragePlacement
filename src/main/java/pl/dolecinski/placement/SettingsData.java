package pl.dolecinski.placement;

public class SettingsData {
	private int k = 0;
	private int rd = 1;
	private int sd = 1;
	private int rq = 1;
	private int sq = 1;
	private double alfa = 0.5;

	private boolean blackRoot;
	private int algo;

	private int levels;
	private int kRegular;

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

	public int getSd() {
		return sd;
	}

	public void setSd(int sd) {
		this.sd = sd;
	}

	public int getRq() {
		return rq;
	}

	public void setRq(int rq) {
		this.rq = rq;
	}

	public int getSq() {
		return sq;
	}

	public void setSq(int sq) {
		this.sq = sq;
	}

	public double getAlfa() {
		return alfa;
	}

	public void setAlfa(double alfa) {
		this.alfa = alfa;
	}

	public boolean isBlackRoot() {
		return blackRoot;
	}

	public void setBlackRoot(boolean blackRoot) {
		this.blackRoot = blackRoot;
	}

	public int getAlgo() {
		return algo;
	}

	public void setAlgo(int algo) {
		this.algo = algo;
	}

	public int getLevels() {
		return levels;
	}

	public void setLevels(int levels) {
		this.levels = levels;
	}

	public int getkRegular() {
		return kRegular;
	}

	public void setkRegular(int kRegular) {
		this.kRegular = kRegular;
	}

}
