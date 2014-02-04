package pl.dolecinski.placement.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import pl.dolecinski.placement.OptionsPresenter;
import pl.dolecinski.placement.SettingsData;

public class OptionsView extends JPanel {

	private String[] alhoritms = new String[] { "WW Algorithm Ver. 2",
			"Bo Sheng Algorithm ", "WW Algorithm Ver. 1", };

	private static final long serialVersionUID = -5437030208681748433L;

	private JFormattedTextField kNumberField;
	private JFormattedTextField rdField;
	private JFormattedTextField sdField;
	private JFormattedTextField alfaField;
	private JFormattedTextField rqField;
	private JFormattedTextField sqField;

	private JFormattedTextField levelOfTreeField;
	private JFormattedTextField kRegOfTreeField;

	private JCheckBox blackRootCombo;
	private JComboBox<String> algoChooseCombo = new JComboBox<String>(alhoritms);

	private JButton calcButton = new JButton("Calculate");

	private ActionListener listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			settingsData.setK(((Number) kNumberField.getValue()).intValue());
			settingsData.setRd(((Number) rdField.getValue()).intValue());
			settingsData.setSd(((Number) sdField.getValue()).intValue());
			settingsData.setRq(((Number) rqField.getValue()).intValue());
			settingsData.setSq(((Number) sqField.getValue()).intValue());
			settingsData.setAlfa(((Number) alfaField.getValue()).doubleValue());

			settingsData.setBlackRoot(blackRootCombo.isSelected());
			settingsData.setAlgo(algoChooseCombo.getSelectedIndex());

			settingsData.setLevels(((Number) levelOfTreeField.getValue())
					.intValue());
			settingsData.setkRegular(((Number) kRegOfTreeField.getValue())
					.intValue());

			optionsPresenter.perform();
		}
	};

	private SettingsData settingsData;

	private OptionsPresenter optionsPresenter;

	public OptionsView(OptionsPresenter optionsPresenter,
			SettingsData settingsData) {
		this.optionsPresenter = optionsPresenter;
		this.settingsData = settingsData;
		this.setLayout(new MigLayout());

		// rd field
		this.add(new JLabel("Data Freq (rd):"), "skip");
		rdField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rdField.setValue(1);
		this.add(rdField, "w 50:50:50");

		this.add(new JLabel("(int)"), "wrap");

		// sd field
		this.add(new JLabel("Data Size (sd):"), "skip");
		sdField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		sdField.setValue(1);
		this.add(sdField, "w 50:50:50");
		this.add(new JLabel("(int)"), "wrap");

		// rq field
		this.add(new JLabel("Query Freq (rq):"), "skip");
		rqField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rqField.setValue(1);
		this.add(rqField, "w 50:50:50");
		this.add(new JLabel("(int)"), "wrap");

		// sq field
		this.add(new JLabel("Query Size (sq):"), "skip");
		sqField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		sqField.setValue(1);
		this.add(sqField, "w 50:50:50");
		this.add(new JLabel("(int)"), "wrap");

		// alfa field
		this.add(new JLabel("Compression (alpha):"), "skip");
		alfaField = new JFormattedTextField(NumberFormat.getNumberInstance());
		alfaField.setValue(0.5);
		alfaField.setInputVerifier(new InputVerifier() {

			@Override
			public boolean verify(JComponent input) {
				if (input instanceof JFormattedTextField) {
					JFormattedTextField ftf = (JFormattedTextField) input;
					AbstractFormatter formatter = ftf.getFormatter();
					if (formatter != null) {
						String text = ftf.getText();
						try {
							double stringToValue = ((Number) formatter
									.stringToValue(text)).doubleValue();
							if (stringToValue <= 0 || stringToValue > 1) {
								input.setBackground(Color.RED);
								return false;
							}
						} catch (ParseException pe) {
							input.setBackground(Color.RED);
							return false;
						}
					}
				}
				input.setBackground(Color.WHITE);
				return true;
			}
		});
		this.add(alfaField, "w 50:50:50");
		this.add(new JLabel("(0, 1]"), "wrap");

		this.add(new JLabel("Algorithm:"), "skip");
		this.add(algoChooseCombo, "span, growx");

		// black nodes field
		this.add(new JLabel("# Storage nodes (k):"), "skip");
		NumberFormat integerInstance = NumberFormat.getIntegerInstance();
		kNumberField = new JFormattedTextField(integerInstance);
		kNumberField.setInputVerifier(new InputVerifier() {

			@Override
			public boolean verify(JComponent input) {
				if (input instanceof JFormattedTextField) {
					JFormattedTextField ftf = (JFormattedTextField) input;
					AbstractFormatter formatter = ftf.getFormatter();
					if (formatter != null) {
						String text = ftf.getText();
						try {
							int stringToValue = ((Number) formatter
									.stringToValue(text)).intValue();
							if (stringToValue <= 0) {
								input.setBackground(Color.RED);
								return false;
							}
						} catch (ParseException pe) {
							input.setBackground(Color.RED);
							return false;
						}
					}
				}
				input.setBackground(Color.WHITE);
				return true;
			}
		});
		kNumberField.setValue(3);
		this.add(kNumberField, "w 50:50:50");
		this.add(new JLabel(" > 0"), "wrap");
		// black root
		this.add(new JLabel("Storage root?"), "skip");
		blackRootCombo = new JCheckBox();
		blackRootCombo.setSelected(true);
		this.add(blackRootCombo, "span, growx");

		this.add(new JLabel("Number of children:"), "skip");
		kRegOfTreeField = new JFormattedTextField(
				NumberFormat.getIntegerInstance());
		kRegOfTreeField.setValue(3);
		this.add(kRegOfTreeField, "w 50:50:50, wrap");

		this.add(new JLabel("Levels:"), "skip");
		levelOfTreeField = new JFormattedTextField(
				NumberFormat.getIntegerInstance());
		levelOfTreeField.setValue(3);
		this.add(levelOfTreeField, "w 50:50:50, wrap");

		this.add(calcButton, "south, wrap");

		calcButton.addActionListener(listener);
	}
}
