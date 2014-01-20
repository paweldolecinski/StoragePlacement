package pl.dolecinski.placement.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
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
import pl.dolecinski.placement.util.Pair;
import pl.dolecinski.placement.util.Permutations;

public class WWAlgorithmVer2 implements AlgorithmExecutor {

	private int k;
	SensorNetwork network;

	private double[][][] C;
	private double[][] C2;
	private int[][] C2_W;
	private List<Integer>[][][] C_BPerm;
	private List<Integer>[][][] C_WPerm;

	private double alfa;
	private int rq;
	private int rd;
	private int sd;
	private int sq;
	private int networkSize;

	public void execute(SensorNetwork network, int k, boolean blackRoot) {
		init(network, k);

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
			// if (network.getRoot().equals(vertex)) {
			// continue;
			// }
			visit(vertex);
		}

		Pair<Integer, Integer> best = processRoot(blackRoot);

		Queue<Pair<Sensor, Pair<Integer, Integer>>> queue = new LinkedList<Pair<Sensor, Pair<Integer, Integer>>>();
		queue.add(new Pair<Sensor, Pair<Integer, Integer>>(network.getRoot(),
				best));

		while (!queue.isEmpty()) {
			Pair<Sensor, Pair<Integer, Integer>> pair = queue.poll();
			Sensor data = pair.left;
			Pair<Integer, Integer> b_w = pair.right;

			data.setType(b_w.right == 0 ? SensorType.STORAGE
					: SensorType.NORMAL);

			List<Integer> b_perm = C_BPerm[data.getId()][b_w.left][b_w.right];

			List<Integer> w_perm = C_WPerm[data.getId()][b_w.left][b_w.right];

			List<Sensor> children = network.getChildrenOfNode(data);
			for (int j = 0; j < children.size(); j++) {
				Integer b = b_perm.get(j);
				Integer w;
				Sensor child = children.get(j);
				if (b_w.right == 0 || b_w.left == 0) {
					w = C2_W[child.getId()][b];
				} else {
					w = w_perm.get(j);
				}
				queue.add(new Pair<Sensor, Pair<Integer, Integer>>(child,
						new Pair<Integer, Integer>(b, w)));
			}
		}

	}

	public void visit(Sensor node) {
		int id = node.getId();

		if (network.isLeaf(node)) {
			// leaves
			C[id][0][1] = 0;
			C[id][1][0] = 0;
			C2[id][1] = 1;
			C2_W[id][1] = 0;
			C2[id][0] = 1;
			C2_W[id][0] = 1;
		} else {
			int Ti = network.subTreeSize(node);
			int min = Math.min(k, Ti);
			for (int B = 0; B < min + 1; B++) {
				if (B == 0) {
					C[id][B][Ti] = calculateSubTreeCost(node, B, Ti);
				} else {
					for (int w = 0; w <= Ti - B; w++) {
						if (w == 0) {
							if (Ti == B) {
								C[id][B][w] = calculateCiTi0(node, Ti);
							} else {

								C[id][B][w] = calculateCiB0(node, B, Ti);
							}
						} else if (w > 0) {
							C[id][B][w] = calculateCiBW(node, B, w, Ti);
						}
					}

				}
				calculateC2(node, B, Ti);
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
		C2 = new double[networkSize][k + 1];
		C2_W = new int[networkSize][k + 1];
		C_BPerm = new List[networkSize][k + 1][networkSize + 1];
		C_WPerm = new List[networkSize][k + 1][networkSize + 1];

		C = new double[networkSize][k + 1][networkSize + 1];

		for (int i = 0; i < networkSize; i++) {
			for (int j = 0; j < k + 1; j++) {
				C2[i][j] = Double.MAX_VALUE;
				for (int w = 0; w < networkSize + 1; w++) {
					C[i][j][w] = Double.MAX_VALUE;
				}
			}
		}
	}

	private Pair<Integer, Integer> processRoot(boolean blackRoot) {
		int best_k = 0;
		int best_w = 0;

		Sensor root = network.getRoot();
		int Ti = network.subTreeSize(root);
		double best_cost = Double.MAX_VALUE;

		for (int B = 1; B <= k; B++) {
			if (blackRoot) {
				if (C[root.getId()][B][0] < best_cost) {
					best_cost = C[root.getId()][B][0];
					best_k = B;
				}

			} else {
				for (int w = 1; w <= Ti - B; w++) {
					if (C[root.getId()][B][w] < best_cost) {
						best_cost = C[root.getId()][B][w];
						best_k = B;
						best_w = w;
					}
				}
			}

		}

		// root.setType(blackRoot ? SensorType.STORAGE : SensorType.NORMAL);

		return new Pair<Integer, Integer>(best_k, best_w);
	}

	private void calculateC2(Sensor node, int B, int Ti) {
		double min = Double.MAX_VALUE;
		int min_w = Integer.MAX_VALUE;
		int m_function = m_function(B);
		for (int w = 0; w <= Ti - B; w++) {
			double sum_tmp = 0;

			sum_tmp += C[node.getId()][B][w] + w * rd * sd + (Ti - w) * alfa
					* rq * sd + m_function * rq * sq;
			if (min > sum_tmp) {
				min = sum_tmp;
				min_w = w;
			}
		}
		C2[node.getId()][B] = min;
		C2_W[node.getId()][B] = min_w;
	}

	private double calculateCiTi0(Sensor node, int Ti) {
		List<Sensor> children = network.getChildrenOfNode(node);
		int delta_i = children.size();
		double sum = 0;
		List<Integer> min_perm = new ArrayList<Integer>();
		for (int j = 0; j < delta_i; j++) {
			Sensor sensor = children.get(j);
			int Tij = network.subTreeSize(sensor);
			min_perm.add(Tij);
			sum += C[sensor.getId()][Tij][0];
		}
		sum += (Ti - 1) * alfa * rq * sd + delta_i * rq * sq;
		C_BPerm[node.getId()][Ti][0] = min_perm;
		return sum;
	}

	private double calculateCiB0(Sensor node, int B, int Ti) {
		List<Sensor> children = network.getChildrenOfNode(node);
		int degree = children.size();
		Set<List<Integer>> perm_B = Permutations.getPermutations(B - 1, degree);

		double min = Double.MAX_VALUE;
		List<Integer> min_perm = null;

		for (List<Integer> p_b : perm_B) {
			double sum_tmp = 0;
			for (int j = 0; j < children.size(); j++) {
				sum_tmp += C2[children.get(j).getId()][p_b.get(j)];
			}
			if (min > sum_tmp) {
				min = sum_tmp;
				min_perm = p_b;
			}
		}

		C_BPerm[node.getId()][B][0] = min_perm;
		return min;
	}

	private double calculateCiBW(Sensor node, int B, int W, int Ti) {
		List<Sensor> children = network.getChildrenOfNode(node);
		int degree = children.size();

		Set<List<Integer>> perm_B = Permutations.getPermutations(B, degree);
		Set<List<Integer>> perm_W = Permutations.getPermutations(W - 1, degree);

		double min = Double.MAX_VALUE;
		List<Integer> min_Wperm = null;
		List<Integer> min_Bperm = null;

		for (List<Integer> p_b : perm_B) {
			for (List<Integer> p_w : perm_W) {
				double sum_tmp = 0;
				for (int j = 0; j < children.size(); j++) {
					sum_tmp += C[children.get(j).getId()][p_b.get(j)][p_w
							.get(j)];
				}
				sum_tmp += r_function(p_b) * rq * sq;
				if (min > sum_tmp) {
					min = sum_tmp;
					min_Bperm = p_b;
					min_Wperm = p_w;
				}
			}
		}

		C_BPerm[node.getId()][B][W] = min_Bperm;
		C_WPerm[node.getId()][B][W] = min_Wperm;
		min += (W - 1) * rd * sd + (Ti - W) * alfa * rq * sd;
		return min;
	}

	private double calculateSubTreeCost(Sensor node, int B, int Ti) {
		double res = 0;
		List<Sensor> children = network.getChildrenOfNode(node);

		for (Sensor sensor : children) {
			res += C[sensor.getId()][0][network.subTreeSize(sensor)];
		}
		Integer[] solution = new Integer[children.size()];
		Arrays.fill(solution, 0);

		C_BPerm[node.getId()][B][Ti] = Arrays.asList(solution);
		return res + (Ti - 1) * rd * sd;
	}

	private int m_function(int i) {
		return (i >= 1) ? 1 : 0;
	}

	private int r_function(List<Integer> p_b) {
		int res = 0;
		for (Integer integer : p_b) {
			res += m_function(integer);
		}
		return res;
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
				return C2;
			}
		};
	}

}
