package pl.dolecinski.placement.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import pl.dolecinski.jgrapht.ext.generate.ReverseIterator;
import pl.dolecinski.placement.network.Sensor;
import pl.dolecinski.placement.network.Sensor.SensorType;
import pl.dolecinski.placement.network.SensorNetwork;
import pl.dolecinski.placement.util.Pair;
import pl.dolecinski.placement.util.Permutations;

public class WWAlgorithmVer1 implements AlgorithmExecutor {

	private int k;
	SensorNetwork network;

	private double[][] C;
	private double[][] W;
	private boolean[][] Color;
	private List<Integer>[][] Perm;

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

		Queue<Pair<Sensor, Integer>> queue = new LinkedList<Pair<Sensor, Integer>>();
		queue.add(new Pair<Sensor, Integer>(network.getRoot(), best_k));

		while (!queue.isEmpty()) {
			Pair<Sensor, Integer> pair = queue.poll();
			Sensor data = pair.left;

			data.setType(Color[data.getId()][pair.right] ? SensorType.STORAGE
					: SensorType.NORMAL);

			List<Integer> perm = Perm[data.getId()][pair.right];

			List<Sensor> children = network.getChildrenOfNode(data);
			// if (perm != null) {
			for (int j = 0; j < children.size(); j++) {
				queue.add(new Pair<Sensor, Integer>(children.get(j), perm
						.get(j)));
			}
			// }
		}
	}

	public void visit(Sensor node) {
		int id = node.getId();

		if (network.isLeaf(node)) {
			// leaves
			C[id][0] = 0;
			W[id][0] = 1;
			C[id][1] = 0;
			W[id][1] = 0;
		} else {
			int Ti = network.subTreeSize(node);
			int min = Math.min(k, Ti);
			for (int B = 0; B < min + 1; B++) {
				Pair<Double, List<Integer>> cb_pair;
				if (B > 0) {
					cb_pair = calculateCWB(node, B - 1);
				} else {
					cb_pair = new Pair<Double, List<Integer>>(Double.MAX_VALUE,
							null);
				}
				Pair<Double, List<Integer>> cw_pair = calculateCWB(node, B);
				if ((cb_pair.left <= cw_pair.left || cw_pair.left == Double.MAX_VALUE)) {
					C[id][B] = cb_pair.left;
					W[id][B] = 0;
					Color[id][B] = true;
					Perm[id][B] = cb_pair.right;
				} else {
					C[id][B] = cw_pair.left;
					double ww_sum = 0;
					List<Sensor> children = network.getChildrenOfNode(node);
					for (int j = 0; j < children.size(); j++) {
						ww_sum += W[children.get(j).getId()][cw_pair.right
								.get(j)];
					}
					W[id][B] = 1 + ww_sum;
					Color[id][B] = false;
					Perm[id][B] = cw_pair.right;

				}
			}

		}

	}

	@SuppressWarnings("unchecked")
	private void init(SensorNetwork network, int k) {
		this.network = network;
		this.k = k;

		alfa = network.getDataCompression();
		rq = network.getQueryFreq();
		rd = network.getDataFreq();
		sd = network.getDataSize();
		sq = network.getQuerySize();
		networkSize = network.getNetworkSize();

		C = new double[networkSize][k + 1];
		W = new double[networkSize][k + 1];
		Color = new boolean[networkSize][k + 1];
		Perm = new List[networkSize][k + 1];

		for (int i = 0; i < networkSize; i++) {
			for (int j = 0; j < k + 1; j++) {
				C[i][j] = Double.MAX_VALUE;
				W[i][j] = 0;
			}
		}
	}

	private int processRoot(boolean blackRoot) {
		int best_k = 0;
		Sensor root = network.getRoot();
		int Ti = network.subTreeSize(root);
		int min = Math.min(k, Ti);
		double best_cost = Double.MAX_VALUE;
		for (int B = 0; B < min + 1; B++) {
			Pair<Double, List<Integer>> min_pair;

			if (blackRoot) {
				min_pair = calculateCWB(root, B - 1);
				Color[root.getId()][B] = true;
			} else {
				min_pair = calculateCWB(root, B);
				Color[root.getId()][B] = false;
			}
			if (C[root.getId()][B] <= best_cost) {
				best_cost = C[root.getId()][B];
				best_k = B;
			}

			C[root.getId()][B] = min_pair.left;
			Perm[root.getId()][B] = min_pair.right;

		}
		return best_k;
	}

	private Pair<Double, List<Integer>> calculateCWB(Sensor node, int B) {
		List<Sensor> children = Graphs.successorListOf(network, node);
		int degree = children.size();
		Pair<Double, List<Integer>> cb_pair = new Pair<Double, List<Integer>>(
				Double.MAX_VALUE, new ArrayList<Integer>(degree));
		Set<List<Integer>> permutations = Permutations.getPermutations(B,
				degree);
		for (List<Integer> perm : permutations) {
			double sum = calculateSubTreeCost(perm, children);

			if (sum <= cb_pair.left) {
				cb_pair.left = sum;
				cb_pair.right = perm;
			}
		}
		return cb_pair;
	}

	private double calculateSubTreeCost(List<Integer> perm,
			List<Sensor> children) {

		double sum = 0;
		for (int j = 0; j < children.size(); j++) {
			Sensor node = children.get(j);
			int id_j = node.getId();
			Integer perm_j = perm.get(j);

			int Trootj = network.subTreeSize(node);

			double w = W[id_j][perm_j];
			sum += C[id_j][perm_j] + w * rd * sd + m_function(perm_j) * rq * sq
					+ (Trootj - w) * alfa * rq * sd;
		}
		return sum;
	}

	private int m_function(int i) {
		return (i >= 1) ? 1 : 0;
	}

	@Override
	public Results getResults() {
		return new WhiteWhiteResults() {

			@Override
			public double[][] getW() {
				return W;
			}

			@Override
			public double[][] getC() {
				return C;
			}
		};
	}

	public abstract class WhiteWhiteResults implements
			AlgorithmExecutor.Results {

		public abstract double[][] getC();

		public abstract double[][] getW();
	}

}
