package pl.dolecinski.placement.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import pl.dolecinski.jgrapht.ext.generate.ReverseIterator;
import pl.dolecinski.placement.network.Sensor;
import pl.dolecinski.placement.network.Sensor.SensorType;
import pl.dolecinski.placement.network.SensorNetwork;
import pl.dolecinski.placement.util.Combinatorics;
import pl.dolecinski.placement.util.Pair;
import pl.dolecinski.placement.util.Permutations;

public class ArticleAlgorithm implements AlgorithmExecutor {

	private int k;
	private double[][][] Ei;
	private Pair<Boolean, List<Integer>>[][][] color_perm;

	SensorNetwork network;

	private double alfa;
	private int rq;
	private int rd;
	private int sd;
	private int sq;
	private int networkSize;

	@SuppressWarnings("unchecked")
	public void execute(SensorNetwork network, int k, boolean blackRoot) {
		this.network = network;
		this.k = k;

		alfa = network.getDataCompression();
		rq = network.getQueryFreq();
		rd = network.getDataFreq();
		sd = network.getDataSize();
		sq = network.getQuerySize();
		networkSize = network.getNetworkSize();

		Ei = new double[networkSize][k + 1][networkSize];
		color_perm = new Pair[networkSize][k + 1][networkSize];

		if (alfa * rq >= rd) {
			network.getRoot().setType(SensorType.STORAGE);
			System.out.println("Lack of calculation: alfa * rq >= rd");
			return;
		}

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

		Queue<Pair<Sensor, Pair<Integer, Integer>>> queue = new LinkedList<Pair<Sensor, Pair<Integer, Integer>>>();
		queue.add(new Pair<Sensor, Pair<Integer, Integer>>(network.getRoot(),
				new Pair<Integer, Integer>(best_k, 0)));

		while (!queue.isEmpty()) {
			Pair<Sensor, Pair<Integer, Integer>> pair = queue.poll();
			Sensor data = pair.left;
			Pair<Integer, Integer> k_l = pair.right;

			Integer kk = k_l.left;
			Integer ll = k_l.right;
			Pair<Boolean, List<Integer>> c_p = color_perm[data.getId()][kk][ll];

			Boolean is_black = c_p.left;
			List<Integer> perm = c_p.right;

			data.setType(is_black ? SensorType.STORAGE : SensorType.NORMAL);

			List<Sensor> children = network.getChildrenOfNode(data);
			// if (perm != null) {
			for (int j = 0; j < children.size(); j++) {

				Pair<Integer, Integer> pp = new Pair<Integer, Integer>(
						perm.get(j), is_black ? 0 : ll + 1);

				queue.add(new Pair<Sensor, Pair<Integer, Integer>>(children
						.get(j), pp));
			}
			// }
		}

	}

	public void visit(Sensor node) {

		int d = network.getLevel(node);

		for (int m = 0; m <= k; m++) {
			for (int l = 0; l <= networkSize - 2; l++) {

				if (network.isLeaf(node)) {
					// leaves
					if (m == 0) {
						Ei[node.getId()][m][l] = (l + 1) * rd * sd + (d - l)
								* rq * alfa * sd;
						color_perm[node.getId()][m][l] = new Pair<Boolean, List<Integer>>(
								false, null);
					} else if (m >= 1) {
						Ei[node.getId()][m][l] = (d + 1) * rq * alfa * sd;
						color_perm[node.getId()][m][l] = new Pair<Boolean, List<Integer>>(
								true, null);
					}
				} else {
					// remaining nodes
					Pair<Double, List<Integer>> minPermSum1 = nodesPartition(
							node, m, l + 1);
					// double b = (1 + network.getChildrenOfNode(node).size()) /
					// 2;
					double min1 = minPermSum1.left + (l + 1) * rd * sd
							+ (d - l) * rq * alfa * sd;// + ((m >= 1) ? b * rq *
														// sq : 0);

					Pair<Double, List<Integer>> minPermSum2 = nodesPartition(
							node, m - 1, 0);

					double min2 = minPermSum2.left + (d + 1) * rq * alfa * sd;
					// + ((m - 1 >= 1) ? b * rq * sq : 0);

					if (m == 0 || min1 <= min2) {
						Ei[node.getId()][m][l] = min1;
						color_perm[node.getId()][m][l] = new Pair<Boolean, List<Integer>>(
								false, minPermSum1.right);
					} else {
						Ei[node.getId()][m][l] = min2;
						color_perm[node.getId()][m][l] = new Pair<Boolean, List<Integer>>(
								true, minPermSum2.right);
					}

				}
			}
		}

	}

	private Pair<Double, List<Integer>> nodesPartition(Sensor node, int m, int l) {
		if (m < 0) {
			m = 0;
		}
		int degree = network.getChildrenOfNode(node).size();
		Set<List<Integer>> numberPartition = Combinatorics.numberPartition(m,
				degree);
		Set<List<Integer>> permutations = new HashSet<List<Integer>>();

		for (List<Integer> list : numberPartition) {
			Permutations p = new Permutations(list);
			while (p.next()) {
				permutations.add(p.getPermutation());
			}
		}
		double minPermSum = Double.MAX_VALUE;
		List<Integer> minPerm = null;
		for (List<Integer> perm : permutations) {
			double permSum = 0;
			List<Sensor> children = network.getChildrenOfNode(node);
			for (int i = 0; i < children.size(); i++) {
				permSum += Ei[children.get(i).getId()][perm.get(i)][l]
						+ r_function(perm);
			}
			if (minPermSum > permSum) {
				minPermSum = permSum;
				minPerm = perm;
			}
		}
		return new Pair<Double, List<Integer>>(minPermSum, minPerm);
	}

	private int processRoot(boolean blackRoot) {
		Sensor root = network.getRoot();
		double best_cost = Double.MAX_VALUE;
		int best_k = k;

		// double b = (1 + network.getChildrenOfNode(network.getRoot()).size())
		// / 2;
		// double Q0 = (k - 1 >= 1) ? b * rq * sq : 0;

		for (int m = 1; m <= k; m++) {
			Pair<Double, List<Integer>> minPermSum = nodesPartition(root,
					m - 1, 0);

			double En = minPermSum.left + rq * alfa * sd;// + Q0;
			Ei[root.getId()][m][0] = En;
			color_perm[root.getId()][m][0] = new Pair<Boolean, List<Integer>>(
					true, minPermSum.right);

			if (En < best_cost) {
				best_cost = En;
				best_k = m;
			}

		}

		return best_k;
	}

	private int m_function(int i) {
		return (i >= 1) ? 1 : 0;
	}

	private int r_function(List<Integer> p_b) {
		int res = 0;
		for (Integer integer : p_b) {
			res += m_function(integer);
		}
		return res * rq * sq;
	}

	@Override
	public Results getResults() {
		return new ArticleResults() {

			public double[][][] getC() {
				return Ei;
			}
		};
	}

	public abstract class ArticleResults implements AlgorithmExecutor.Results {

		public abstract double[][][] getC();

	}

}
