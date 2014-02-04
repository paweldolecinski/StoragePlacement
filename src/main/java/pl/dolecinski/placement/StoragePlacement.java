package pl.dolecinski.placement;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;

import pl.dolecinski.jgrapht.ext.generate.RegularTreeGenerator;
import pl.dolecinski.placement.algorithm.AlgorithmExecutor;
import pl.dolecinski.placement.algorithm.AlgorithmExecutor.Results;
import pl.dolecinski.placement.algorithm.ArticleAlgorithm;
import pl.dolecinski.placement.algorithm.ArticleAlgorithm.ArticleResults;
import pl.dolecinski.placement.algorithm.WWAlgorithmVer1;
import pl.dolecinski.placement.algorithm.WWAlgorithmVer1.WhiteWhiteResults;
import pl.dolecinski.placement.algorithm.WWAlgorithmVer2;
import pl.dolecinski.placement.algorithm.WWResults;
import pl.dolecinski.placement.network.Sensor;
import pl.dolecinski.placement.network.Sensor.SensorType;
import pl.dolecinski.placement.network.SensorNetwork;
import pl.dolecinski.placement.util.Pair;
import pl.dolecinski.placement.view.OptionsView;
import pl.dolecinski.placement.view.ResultsView;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class StoragePlacement extends JFrame implements OptionsPresenter {

	private static final long serialVersionUID = -3579515486027405866L;

	private mxGraphComponent graphComponent;
	private mxGraph graph;

	private JTabbedPane settingsTabbedPane = new JTabbedPane();
	private JTabbedPane graphTabbedPane = new JTabbedPane();

	private OptionsView optionsPanel;
	private ResultsView resultsPanel = new ResultsView();

	private SettingsData settingsData;

	public StoragePlacement() {
		super("Storage Placement");

		initGraphComponent();

		init();

	}

	private void initGraphComponent() {
		graph = new mxGraph();

		graph.setAllowLoops(false);
		graph.setAutoSizeCells(true);
		graph.setVertexLabelsMovable(false);
		graph.setAllowDanglingEdges(false);
		graph.setCellsEditable(false);
		graph.setCellsSelectable(false);
		graph.setEnabled(false);

		graphComponent = new mxGraphComponent(graph);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
		graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_NONE);
		graphComponent.setAutoScroll(false);
		graphComponent.setCenterPage(true);
	}

	private void init() {
		settingsData = new SettingsData();

		optionsPanel = new OptionsView(this, settingsData);
		settingsTabbedPane.addTab("Settings", optionsPanel);

		graphTabbedPane.addTab("Graph", graphComponent);

		resultsPanel.setLayout(new MigLayout());

		JScrollPane jScrollPane = new JScrollPane(resultsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		graphTabbedPane.addTab("Results", jScrollPane);

		JPanel panel = new JPanel(new MigLayout("fill", "[grow,fill]"));

		panel.add(settingsTabbedPane, "grow, west");
		panel.add(graphTabbedPane, "grow");
		getContentPane().add(panel);
	}

	@Override
	public void perform() {
		AlgorithmExecutor algorithm;
		switch (settingsData.getAlgo()) {
		case 0:
			algorithm = new WWAlgorithmVer2();
			break;
		case 1:
			algorithm = new ArticleAlgorithm();
			break;
		case 2:
			algorithm = new WWAlgorithmVer1();
			break;
		default:
			algorithm = new WWAlgorithmVer2();
			break;
		}

		SensorNetwork tree = createStringGraph(settingsData.getLevels(),
				settingsData.getkRegular(), settingsData.getRd(),
				settingsData.getSd(), settingsData.getAlfa(),
				settingsData.getRq(), settingsData.getSq());

		if (tree != null)
			placeStorageNodes(tree, settingsData.getK(), algorithm,
					settingsData.isBlackRoot());

	}

	public void placeStorageNodes(SensorNetwork network, int k,
			AlgorithmExecutor algorithm, boolean blackRoot) {

		algorithm.execute(network, k, blackRoot);
		showNetwork(network);
		showResults(algorithm.getResults());
	}

	private void showResults(Results results) {
		resultsPanel.removeAll();
		if (results instanceof ArticleResults) {
			ArticleResults res = (ArticleResults) results;
			resultsPanel.add(new JLabel("Table C[i][m][l] of costs"),
					"north, wrap");
			double[][][] c = res.getC();
			for (int i = 0; i < c.length; i++) {
				resultsPanel.add(new JLabel("Node " + i), "");
				JPanel p = new JPanel();
				p.setLayout(new MigLayout("insets 20"));

				for (int j = 0; j < c[i].length; j++) {

					p.add(new JLabel("m=" + j), "w 40:40:40");

					for (int j2 = 0; j2 < c[i][j].length; j2++) {
						double v = c[i][j][j2];
						String string = "";
						if (v == Double.MAX_VALUE) {
							string += "---";
						} else {
							string += v;

						}
						p.add(new JLabel(string), "width 40:40:60");
					}
					p.add(new JLabel(" "), "wrap");
					p.add(new JLabel(new String(new char[20 * c[i][j].length])
							.replace("\0", "_")), "wrap, span");
				}
				resultsPanel.add(p, "wrap, span");
			}

		} else if (results instanceof WWResults) {
			WWResults res = (WWResults) results;
			resultsPanel.add(new JLabel("Table C[i][B][W] of costs"),
					"north, wrap");
			double[][][] c = res.getC();
			for (int i = 0; i < c.length; i++) {
				resultsPanel.add(new JLabel("Node " + i), "");
				JPanel p = new JPanel();
				p.setLayout(new MigLayout("insets 20"));

				for (int j = 0; j < c[i].length; j++) {

					p.add(new JLabel("B=" + j), "w 40:40:40");

					for (int j2 = 0; j2 < c[i][j].length; j2++) {
						double v = c[i][j][j2];
						String string = "";
						if (v == Double.MAX_VALUE) {
							string += "---";
						} else {
							string += v;

						}
						p.add(new JLabel(string), "width 40:40:60");
					}
					p.add(new JLabel(" "), "wrap");
					p.add(new JLabel(new String(new char[20 * c[i][j].length])
							.replace("\0", "_")), "wrap, span");
				}
				resultsPanel.add(p, "wrap, span");
			}

			resultsPanel.add(new JLabel("Table C'[i][B] of costs"),
					"wrap, span");
			double[][] c2 = res.getC2();
			for (int i = 0; i < c2.length; i++) {
				resultsPanel.add(new JLabel("Node " + i), "skip,  w 70:70:100");

				for (int j = 0; j < c2[i].length; j++) {

					double v = c2[i][j];
					String string = "";
					if (v == Double.MAX_VALUE) {
						string += "---";
					} else {
						string += v;

					}
					resultsPanel
							.add(new JLabel(string), "skip, width 40:40:60");

				}
				resultsPanel.add(new JLabel(" "), "wrap");
				resultsPanel.add(new JLabel(new String(
						new char[20 * c2[i].length]).replace("\0", "_")),
						"growx, span, wrap");
			}

		} else if (results instanceof WhiteWhiteResults) {
			WhiteWhiteResults res = (WhiteWhiteResults) results;
			double[][] c = res.getC();
			double[][] w = res.getW();
			resultsPanel.add(new JLabel(" "), "north, growx, wrap, span");
			resultsPanel.add(new JLabel("Table C[i][B] of costs"),
					"north, wrap");
			resultsPanel.add(
					new JLabel(new String(new char[20 * c[0].length]).replace(
							"\0", "_")), "growx, north, wrap");

			for (int i = 0; i < c.length; i++) {
				resultsPanel.add(new JLabel("Node " + i), "skip,  w 70:70:100");

				for (int j = 0; j < c[i].length; j++) {

					double v = c[i][j];
					String string = "";
					if (v == Double.MAX_VALUE) {
						string += "---";
					} else {
						string += v;

					}
					resultsPanel
							.add(new JLabel(string), "skip, width 40:40:60");

				}
				resultsPanel.add(new JLabel(" "), "wrap");
				resultsPanel.add(new JLabel(new String(
						new char[20 * c[i].length]).replace("\0", "_")),
						"growx, span, wrap");
			}

			resultsPanel.add(new JLabel(" "), "growx, wrap, span");
			resultsPanel.add(new JLabel("Table W[i][B]"), "wrap, span");
			resultsPanel.add(
					new JLabel(new String(new char[20 * w[0].length]).replace(
							"\0", "_")), "growx, wrap, span");

			for (int i = 0; i < w.length; i++) {
				resultsPanel.add(new JLabel("Node " + i), "skip,  w 70:70:100");

				for (int j = 0; j < w[i].length; j++) {

					double v = w[i][j];
					String string = "";
					if (v == Double.MAX_VALUE) {
						string += "---";
					} else {
						string += v;

					}
					resultsPanel
							.add(new JLabel(string), "skip, width 40:40:60");

				}
				resultsPanel.add(new JLabel(""), "wrap");
				resultsPanel.add(new JLabel(new String(
						new char[20 * w[i].length]).replace("\0", "_")),
						"growx, span, wrap");
			}
		}
		resultsPanel.revalidate();
	}

	private void showNetwork(SensorNetwork network) {
		System.out.println(network);
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try {
			graph.removeCells(graph.getChildVertices(parent));
			updateModel(network, parent);
			new mxHierarchicalLayout(graph).execute(parent);
		} finally {
			graph.getModel().endUpdate();
		}

	}

	private void updateModel(SensorNetwork network, Object parent) {

		Queue<Pair<Sensor, Object>> queue = new LinkedList<Pair<Sensor, Object>>();
		queue.add(new Pair<Sensor, Object>(network.getRoot(), null));

		while (!queue.isEmpty()) {
			Pair<Sensor, Object> node = queue.poll();
			SensorType type = node.left.getType();
			String name = "v" + node.left.getId();
			Object v1 = graph
					.insertVertex(
							parent,
							null,
							name,
							0,
							0,
							60,
							30,
							"ROUNDED;shape=ellipse;fontStyle=1;"
									+ (type.equals(SensorType.STORAGE) ? "strokeColor=white;fontColor=white;fillColor=black"
											: "strokeColor=black;fontColor=black;fillColor=white"));
			if (node.right != null) {
				graph.insertEdge(parent, null, "", node.right, v1);
			}
			List<Sensor> children = network.getChildrenOfNode(node.left);

			for (int j = 0; j < children.size(); j++) {
				queue.add(new Pair<Sensor, Object>(children.get(j), v1));
			}
		}
	}

	private static SensorNetwork createStringGraph(int levels, int kregular,
			int rd, int sd, double alfa, int rq, int sq) {

		SensorNetwork g = new SensorNetwork(rd, sd, alfa, rq, sq);
		VertexFactory<Sensor> vertexFactory = new VertexFactory<Sensor>() {
			private int i;

			public Sensor createVertex() {
				return new Sensor(i++);
			}
		};

		Map<String, Sensor> resultMap = new HashMap<String, Sensor>();
		GraphGenerator<Sensor, DefaultEdge, Sensor> gen;

		gen = new RegularTreeGenerator<Sensor, DefaultEdge>(levels, kregular);
		gen.generateGraph(g, vertexFactory, resultMap);
		Sensor startVertex = resultMap.get(RegularTreeGenerator.START_VERTEX);
		g.setRoot(startVertex);

		return g;
	}

}
