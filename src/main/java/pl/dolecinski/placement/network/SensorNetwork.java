package pl.dolecinski.placement.network;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

public class SensorNetwork extends ListenableDirectedGraph<Sensor, DefaultEdge> {

	private static final long serialVersionUID = -5170323744127786366L;
	private StringBuilder output;

	private int dataFreq; // rd
	private int dataSize; // sd
	private double dataCompression; // alfa

	private int queryFreq; // rq
	private int querySize; // sq

	private Sensor root;

	public SensorNetwork(int dataFreq, int dataSize, double d, int queryFreq,
			int querySize) {
		super(DefaultEdge.class);
		// this.root = root;
		this.dataFreq = dataFreq;
		this.dataSize = dataSize;
		this.dataCompression = d;
		this.queryFreq = queryFreq;
		this.querySize = querySize;

	}

	public void setRoot(Sensor root) {
		this.root = root;
	}

	public Sensor getRoot() {
		return root;
	}

	public int getNetworkSize() {
		return this.vertexSet().size();
	}

	public int getDataFreq() {
		return dataFreq;
	}

	public int getDataSize() {
		return dataSize;
	}

	public double getDataCompression() {
		return dataCompression;
	}

	public int getQueryFreq() {
		return queryFreq;
	}

	public int getQuerySize() {
		return querySize;
	}

	public List<Sensor> getChildrenOfNode(Sensor node) {
		return Graphs.successorListOf(this, node);
	}

	public int subTreeSize(Sensor n) {
		int size = 1;

		Queue<Sensor> queue = new LinkedList<Sensor>();
		queue.add(n);

		while (!queue.isEmpty()) {
			Sensor node = queue.poll();
			List<Sensor> predecessorListOf = Graphs.successorListOf(this, node);

			size += predecessorListOf.size();
			for (int j = 0; j < predecessorListOf.size(); j++) {
				queue.add(predecessorListOf.get(j));
			}
		}
		return size;
	}

	public boolean isLeaf(Sensor node) {
		return getChildrenOfNode(node).isEmpty();
	}

	public int getLevel(Sensor n) {
		if (n == getRoot())
			return 0;
		else
			return 1 + getLevel(Graphs.predecessorListOf(this, n).get(0));
	}

	private void makeTreeStringOutline(Sensor n) {
		for (int i = 0; i < getLevel(n); i++)
			output.append("\t");
		output.append(n + "\n");
		List<Sensor> temp = getChildrenOfNode(n);
		for (Sensor sensor : temp) {
			makeTreeStringOutline(sensor);
		}
	}

	@Override
	public String toString() {
		output = new StringBuilder();

		makeTreeStringOutline(root);

		return output.toString();
	}
}
