package pl.dolecinski.placement.algorithm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import pl.dolecinski.jgrapht.ext.generate.ReverseIterator;
import pl.dolecinski.placement.network.Sensor;
import pl.dolecinski.placement.network.Sensor.SensorType;
import pl.dolecinski.placement.network.SensorNetwork;

public class WWAlgorithmPath implements AlgorithmExecutor {

	private int k;
	SensorNetwork network;

	private double[][][] C;

	private double alfa;
	private int rq;
	private int rd;
	private int sd;
	private int sq;
	private int networkSize;

	public void execute(SensorNetwork network, int k, boolean blackRoot) {
		init(network, k);

		Iterator<Sensor> iter = new ReverseIterator<Sensor>(
				new DepthFirstIterator<Sensor, DefaultEdge>(network));

		Sensor vertex;
		while (iter.hasNext()) {
			vertex = iter.next();
			if (network.getRoot().equals(vertex)) {
				continue;
			}
			visit(vertex);
		}

		int best_k = processRoot(blackRoot);
		List<Sensor> children = network.getChildrenOfNode(network.getRoot());
		Queue<Sensor> queue = new LinkedList<Sensor>();
		queue.add(children.get(0));
		int w = 0;
		boolean searchW = true;
		while (!queue.isEmpty()) {
			Sensor s = queue.poll();
			if (searchW) {
				double min_cost = Double.MAX_VALUE;
				int max_w = networkSize;
				for (int tmp_w = 0; tmp_w < networkSize + 1; tmp_w++) {
					if (min_cost > C[s.getId()][best_k][tmp_w]) {
						min_cost = C[s.getId()][best_k][tmp_w];
						max_w = tmp_w;
					}
				}
				w = max_w;
				searchW = false;
			}
			if (w == 0) {
				s.setType(SensorType.STORAGE);
				best_k--;
				searchW = true;
			} else {
				s.setType(SensorType.NORMAL);
				w--;
			}
			List<Sensor> ch = network.getChildrenOfNode(s);
			// if (perm != null) {
			for (int j = 0; j < ch.size(); j++) {
				queue.add(ch.get(j));
			}
		}
	}

	public void visit(Sensor node) {
		int id = node.getId();

		if (network.isLeaf(node)) {
			// leaves
			C[id][0][1] = 0;
			C[id][1][0] = 0;
			// W[id][0] = 1;
			// C[id][1] = 0;
			// W[id][1] = 0;
		} else {
			int Ti = network.subTreeSize(node);
			int min = Math.min(k, Ti);
			List<Sensor> children = network.getChildrenOfNode(node);

			for (int B = 0; B < min + 1; B++) {
				for (int w = 0; w <= Ti - B; w++) {

					if (w == 0 && B > 0) {
						C[id][B][w] = calculateCWB(children.get(0), B, Ti);
					} else if (w > 0) {
						C[id][B][w] = calculateSubTreeCost(children.get(0), B,
								w, Ti);
					}

				}
			}

		}

	}

	private void init(SensorNetwork network, int k) {
		this.network = network;
		this.k = k;

		alfa = network.getDataCompression();
		rq = network.getQueryFreq();
		rd = network.getDataFreq();
		sd = network.getDataSize();
		sq = network.getQuerySize();
		networkSize = network.getNetworkSize();

		C = new double[networkSize][k + 1][networkSize + 1];

		for (int i = 0; i < networkSize; i++) {
			for (int j = 0; j < k + 1; j++) {
				for (int w = 0; w < networkSize + 1; w++) {
					C[i][j][w] = Double.MAX_VALUE;
				}
			}
		}
	}

	private int processRoot(boolean blackRoot) {
		int best_k = 0;
		Sensor root = network.getRoot();
		int Ti = network.subTreeSize(root);
		List<Sensor> children = network.getChildrenOfNode(root);
		int min = Math.min(k, Ti);
		double best_cost = Double.MAX_VALUE;
		for (int B = 0; B < min + 1; B++) {
			for (int w = 0; w <= Ti - B; w++) {

				if (w == 0 && B > 0) {
					C[root.getId()][B][w] = calculateCWB(children.get(0), B, Ti);
				} else if (w > 0) {
					C[root.getId()][B][w] = calculateSubTreeCost(
							children.get(0), B, w, Ti);
				}

			}

			if (blackRoot) {
				if (C[root.getId()][B][0] <= best_cost) {
					best_cost = C[root.getId()][B][0];
					best_k = B;
				}

			} else {
				for (int w = 1; w <= Ti - B; w++) {
					if (C[root.getId()][B][w] <= best_cost) {
						best_cost = C[root.getId()][B][w];
						best_k = B;
					}
				}
			}

		}
		root.setType(blackRoot ? SensorType.STORAGE : SensorType.NORMAL);

		return blackRoot ? best_k - 1 : best_k;
	}

	private double calculateCWB(Sensor node, int B, int Ti) {
		double min = Double.MAX_VALUE;
		for (int w = 0; w <= Ti - B; w++) {
			double tmp = C[node.getId()][B - 1][w] + w * rd * sd
					+ m_function(B - 1) * rq * sq + (Ti - 1 - w) * alfa * rq
					* sd;

			if (tmp <= min) {
				min = tmp;
			}
		}
		return min;
	}

	private double calculateSubTreeCost(Sensor node, int B, int w, int Ti) {

		return C[node.getId()][B][w - 1] + (w - 1) * rd * sd + m_function(B)
				* rq * sq + (Ti - w) * alfa * rq * sd;
	}

	private int m_function(int i) {
		return (i >= 1) ? 1 : 0;
	}

	@Override
	public Results getResults() {
		return new WWResults() {

			@Override
			public double[][][] getC() {
				return C;
			}

			@Override
			public double[][] getC2() {
				return new double[][] {};
			}
		};
	}

}
