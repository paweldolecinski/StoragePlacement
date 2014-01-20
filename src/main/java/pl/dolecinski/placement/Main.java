package pl.dolecinski.placement;

import javax.swing.JFrame;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		StoragePlacement storagePlacement = new StoragePlacement();

		storagePlacement.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		storagePlacement.setSize(800, 600);
		storagePlacement.setExtendedState(JFrame.MAXIMIZED_BOTH);

		storagePlacement.setVisible(true);

	}

}
