package pl.dolecinski.placement;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
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

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class StoragePlacement extends JFrame {

	private static final long serialVersionUID = -3579515486027405866L;

	private String[] alhoritms = new String[] { "WW Algorithm Ver. 2",
			"Article Algorithm ", "WW Algorithm Ver. 1", };

	private mxGraphComponent graphComponent;
	private mxGraph graph;

	private JButton calcButton = new JButton("Calculate");
	private JPanel optionsPanel = new JPanel();
	private JPanel resultsPanel = new JPanel();
	private JTextField kNumberField = new JTextField();
	private JTextField rdField = new JTextField();
	private JTextField sdField = new JTextField();
	private JTextField alfaField = new JTextField();
	private JTextField rqField = new JTextField();
	private JTextField sqField = new JTextField();

	private JTextField nodesNumberField = new JTextField();
	private JTextField levelOfTreeField = new JTextField();
	private JTextField kRegOfTreeField = new JTextField();

	private JComboBox<String> blackRootCombo;
	private JTabbedPane graphTabbedPane = new JTabbedPane();
	private JTabbedPane settingsTabbedPane = new JTabbedPane();
	private JComboBox<String> algoChooseCombo = new JComboBox<String>(alhoritms);

	private ButtonGroup graphTypeButton;

	private JRadioButton pathButton = new JRadioButton("Path");

	private JRadioButton regularTreeButton = new JRadioButton("Regular Tree");

	public StoragePlacement() {
		super("Storage Placement");

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

		init();
		calcButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int k = 0;
				int rd = 1, sd = 1, rq = 1, sq = 1;
				double alfa = 0.5;

				try {
					k = Integer.parseInt(kNumberField.getText());
					rd = Integer.parseInt(rdField.getText());
					sd = Integer.parseInt(sdField.getText());
					rq = Integer.parseInt(rqField.getText());
					sq = Integer.parseInt(sqField.getText());
					alfa = Double.parseDouble(alfaField.getText());
					if (alfa <= 0 || alfa > 1) {
						JOptionPane.showMessageDialog(
								StoragePlacement.this.getContentPane(),
								"Alpha should be (0, 1]");
						return;
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
							StoragePlacement.this.getContentPane(),
							"Incorrect value of k, rd, sd, rq, sq or alpha");
					return;
				}
				if (k <= 0) {
					JOptionPane.showMessageDialog(
							StoragePlacement.this.getContentPane(),
							"k should be > 0");
					return;
				}
				boolean blackRoot = blackRootCombo.getSelectedIndex() == 0;
				int algo = algoChooseCombo.getSelectedIndex();
				AlgorithmExecutor algorithm;
				switch (algo) {
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
				;
				SensorNetwork tree = null;
				boolean treeSelected = true;
				if (pathButton.isSelected()) {
					treeSelected = false;
					int nodes = 0;
					try {
						nodes = Integer.parseInt(nodesNumberField.getText());
					} catch (Exception e) {
						// TODO: handle exception
					}
					if (nodes <= 0) {
						JOptionPane.showMessageDialog(
								StoragePlacement.this.getContentPane(),
								"nodes number should be > 0");
						return;
					}
					tree = createStringGraph(nodes, treeSelected, rd, sd, alfa,
							rq, sq);

				} else {
					int levels = 0;
					int kRegular = 0;
					try {
						levels = Integer.parseInt(levelOfTreeField.getText());
						kRegular = Integer.parseInt(kRegOfTreeField.getText());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(
								StoragePlacement.this.getContentPane(),
								"Incorrect value of levels or children");
						return;
					}
					tree = createStringGraph(levels, kRegular, treeSelected,
							rd, sd, alfa, rq, sq);

				}
				if (tree != null)
					placeStorageNodes(tree, k, algorithm, blackRoot);

			}
		});
	}

	private void init() {
		initOptions();

		graphTabbedPane.addTab("Graph", graphComponent);

		resultsPanel.setLayout(new MigLayout());
		JScrollPane jScrollPane = new JScrollPane(resultsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// JPanel jpanel = new JPanel(new BorderLayout());
		// jpanel.add(jScrollPane);
		graphTabbedPane.addTab("Results", jScrollPane);

		JPanel panel = new JPanel(new MigLayout("fill", "[grow,fill]"));

		panel.add(settingsTabbedPane, "grow, west");
		panel.add(graphTabbedPane, "grow");
		getContentPane().add(panel);
	}

	private void initOptions() {
		// algoChooseCombo.addItemListener(new ItemListener() {
		//
		// @Override
		// public void itemStateChanged(ItemEvent arg0) {
		// int algo = algoChooseCombo.getSelectedIndex();
		// if (algo == 1) {
		// pathButton.setSelected(true);
		// regularTreeButton.setEnabled(false);
		// } else {
		// regularTreeButton.setEnabled(true);
		// regularTreeButton.setSelected(true);
		// }
		// }
		// });

		optionsPanel.setLayout(new MigLayout());

		optionsPanel.add(new JLabel("Data Freq (rd):"), "skip");
		rdField.setText("1");
		optionsPanel.add(rdField, "w 50:50:50");
		optionsPanel.add(new JLabel("(int)"), "wrap");

		optionsPanel.add(new JLabel("Data Size (sd):"), "skip");
		sdField.setText("1");
		optionsPanel.add(sdField, "w 50:50:50");
		optionsPanel.add(new JLabel("(int)"), "wrap");

		optionsPanel.add(new JLabel("Query Freq (rq):"), "skip");
		rqField.setText("1");
		optionsPanel.add(rqField, "w 50:50:50");
		optionsPanel.add(new JLabel("(int)"), "wrap");

		optionsPanel.add(new JLabel("Query Size (sq):"), "skip");
		sqField.setText("1");
		optionsPanel.add(sqField, "w 50:50:50");
		optionsPanel.add(new JLabel("(int)"), "wrap");

		optionsPanel.add(new JLabel("Compression (alpha):"), "skip");
		alfaField.setText("0.5");
		optionsPanel.add(alfaField, "w 50:50:50");
		optionsPanel.add(new JLabel("(0, 1]"), "wrap");

		optionsPanel.add(new JLabel("Algorithm:"), "skip");
		optionsPanel.add(algoChooseCombo, "span, growx");

		optionsPanel.add(new JLabel("# Black nodes (k):"), "skip");
		kNumberField.setText("3");
		optionsPanel.add(kNumberField, "w 50:50:50");

		optionsPanel.add(new JLabel("Black root?"), "skip");
		blackRootCombo = new JComboBox<String>(new String[] { "true", "false" });
		optionsPanel.add(blackRootCombo, "span, growx");

		graphTypeButton = new ButtonGroup();
		optionsPanel.add(new JLabel("Graph type:"), "wrap");
		regularTreeButton.setSelected(true);
		kRegOfTreeField.setEnabled(true);
		levelOfTreeField.setEnabled(true);
		nodesNumberField.setEnabled(false);
		regularTreeButton.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				kRegOfTreeField.setEnabled(regularTreeButton.isSelected());
				levelOfTreeField.setEnabled(regularTreeButton.isSelected());
				nodesNumberField.setEnabled(!regularTreeButton.isSelected());
			}
		});

		graphTypeButton.add(regularTreeButton);
		optionsPanel.add(regularTreeButton, "wrap");

		optionsPanel.add(new JLabel("Number of children:"), "skip");
		kRegOfTreeField.setText("2");
		optionsPanel.add(kRegOfTreeField, "w 50:50:50, wrap");
		optionsPanel.add(new JLabel("Levels:"), "skip");
		levelOfTreeField.setText("3");
		optionsPanel.add(levelOfTreeField, "w 50:50:50, wrap");
		pathButton.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				kRegOfTreeField.setEnabled(!pathButton.isSelected());
				levelOfTreeField.setEnabled(!pathButton.isSelected());
				nodesNumberField.setEnabled(pathButton.isSelected());
			}
		});
		graphTypeButton.add(pathButton);
		optionsPanel.add(pathButton, "wrap");

		optionsPanel.add(new JLabel("Number of Nodes:"), "skip");
		nodesNumberField.setText("7");
		optionsPanel.add(nodesNumberField, "w 50:50:50, growx");

		optionsPanel.add(calcButton, "south, wrap");

		settingsTabbedPane.addTab("Settings", optionsPanel);
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
			boolean treeSelected, int rd, int sd, double alfa, int rq, int sq) {
		return createStringGraph(0, levels, kregular, treeSelected, rd, sd,
				alfa, rq, sq);
	}

	private static SensorNetwork createStringGraph(int nodes,
			boolean treeSelected, int rd, int sd, double alfa, int rq, int sq) {
		return createStringGraph(nodes, 0, 0, treeSelected, rd, sd, alfa, rq,
				sq);
	}

	private static SensorNetwork createStringGraph(int nodes, int levels,
			int kregular, boolean treeSelected, int rd, int sd, double alfa,
			int rq, int sq) {

		SensorNetwork g = new SensorNetwork(rd, sd, alfa, rq, sq);
		VertexFactory<Sensor> vertexFactory = new VertexFactory<Sensor>() {
			private int i;

			public Sensor createVertex() {
				return new Sensor(i++);
			}
		};

		Map<String, Sensor> resultMap = new HashMap<String, Sensor>();
		GraphGenerator<Sensor, DefaultEdge, Sensor> gen;

		if (treeSelected) {
			gen = new RegularTreeGenerator<Sensor, DefaultEdge>(levels,
					kregular);
		} else {
			gen = new LinearGraphGenerator<Sensor, DefaultEdge>(nodes);
		}
		gen.generateGraph(g, vertexFactory, resultMap);
		Sensor startVertex = resultMap.get(RegularTreeGenerator.START_VERTEX);
		g.setRoot(startVertex);
		// Sensor v0 = new Sensor(14, "SINK");
		// Sensor v1 = new Sensor(12);
		// Sensor v2 = new Sensor(13);
		//
		// Sensor v3 = new Sensor(8);
		// Sensor v4 = new Sensor(9);
		// Sensor v5 = new Sensor(10);
		// Sensor v6 = new Sensor(11);
		//
		// Sensor v7 = new Sensor(0);
		// Sensor v8 = new Sensor(1);
		// Sensor v9 = new Sensor(2);
		// Sensor v10 = new Sensor(3);
		// Sensor v11 = new Sensor(4);
		// Sensor v12 = new Sensor(5);
		// Sensor v13 = new Sensor(6);
		// Sensor v14 = new Sensor(7);
		//
		// SensorNetwork g = new SensorNetwork(v0, 1, 1, 0.5, 1, 1);
		// // g.setRoot(v0);
		// // add the vertices
		// g.addVertex(v0);
		// g.addVertex(v1);
		// g.addVertex(v2);
		// g.addVertex(v3);
		// g.addVertex(v4);
		// g.addVertex(v5);
		// g.addVertex(v6);
		// g.addVertex(v7);
		// g.addVertex(v8);
		// g.addVertex(v9);
		// g.addVertex(v10);
		// g.addVertex(v11);
		// g.addVertex(v12);
		// g.addVertex(v13);
		// g.addVertex(v14);
		// //
		// // // add edges to create a circuit
		// g.addEdge(v0, v1);
		// g.addEdge(v0, v2);
		//
		// g.addEdge(v1, v3);
		// g.addEdge(v1, v4);
		//
		// g.addEdge(v2, v5);
		// g.addEdge(v2, v6);
		//
		// g.addEdge(v3, v7);
		// g.addEdge(v3, v8);
		//
		// g.addEdge(v4, v9);
		// g.addEdge(v4, v10);
		//
		// g.addEdge(v5, v11);
		// g.addEdge(v5, v12);
		//
		// g.addEdge(v6, v13);
		// g.addEdge(v6, v14);
		return g;
	}

}
