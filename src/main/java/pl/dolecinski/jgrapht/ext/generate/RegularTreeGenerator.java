package pl.dolecinski.jgrapht.ext.generate;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

public class RegularTreeGenerator<V, E> implements GraphGenerator<V, E, V> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	/**
	 * Role for the first vertex generated.
	 */
	public static final String START_VERTEX = "Start Vertex";

	// ~ Instance fields
	// --------------------------------------------------------

	private int level;
	private int children;

	// ~ Constructors
	// -----------------------------------------------------------

	public RegularTreeGenerator(int level, int children) {

		if (level < 0) {
			throw new IllegalArgumentException("must be non-negative");
		}
		if (children <= 0) {
			throw new IllegalArgumentException("must be non-negative");
		}

		this.children = children;
		this.level = level;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void generateGraph(Graph<V, E> target,
			VertexFactory<V> vertexFactory, Map<String, V> resultMap) {

		V root = vertexFactory.createVertex();
		target.addVertex(root);
		if (resultMap != null) {
			resultMap.put(START_VERTEX, root);
		}

		Queue<V> queue = new LinkedList<V>();

		queue.add(root);
		for (int l = 1; l < level; l++) {
			Queue<V> queue2 = new LinkedList<V>();
			while (!queue.isEmpty()) {
				V current = queue.poll();
				for (int i = 0; i < children; i++) {
					V child = vertexFactory.createVertex();
					target.addVertex(child);
					target.addEdge(current, child);
					queue2.add(child);
				}
			}
			queue = queue2;
		}

	}
}
