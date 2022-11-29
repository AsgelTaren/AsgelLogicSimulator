package asgel.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import com.google.gson.JsonObject;

import asgel.app.model.ModelHolder;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class ExportAsModelBoxDialog extends JDialog {

	public ExportAsModelBoxDialog(JFrame frame, App app, ModelHolder holder) {
		super(frame, "Export as Model Box", true);

		// Number format
		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setAllowsInvalid(false);
		formatter.setMinimum(0);

		// Panel creation

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridheight = gbc.gridwidth = 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Location field
		JTextField locField = new JTextField();
		// Adding labels

		panel.add(new JLabel("Name"), gbc);
		gbc.gridy = 1;
		panel.add(new JLabel("Symbol"), gbc);
		gbc.gridy = 2;
		panel.add(new JLabel("Width"), gbc);
		gbc.gridy = 3;
		panel.add(new JLabel("Height"), gbc);

		// Adding fields

		JTextField name = new JTextField();
		name.setPreferredSize(new Dimension(150, 25));
		gbc.gridx = 1;
		gbc.gridy = 0;
		panel.add(name, gbc);

		JTextField symbol = new JTextField();
		symbol.setPreferredSize(new Dimension(150, 25));
		gbc.gridy = 1;
		panel.add(symbol, gbc);

		JFormattedTextField width = new JFormattedTextField(formatter);
		width.setPreferredSize(new Dimension(150, 25));
		gbc.gridy = 2;
		panel.add(width, gbc);

		JFormattedTextField height = new JFormattedTextField(formatter);
		height.setPreferredSize(new Dimension(150, 25));
		gbc.gridy = 3;
		panel.add(height, gbc);

		// Adding buttons

		JButton export = new JButton("Export");
		export.addActionListener(e -> {

			String url = Utils.askForRelativity(new File(locField.getText()).getParentFile().toPath(),
					holder.getFile().toPath(), app.getWorkingDir().toPath(), app.getJFrame());
			JsonObject json = new JsonObject();
			json.addProperty("name", name.getText());
			json.addProperty("symbol", symbol.getText());
			json.addProperty("width", width.getText());
			json.addProperty("height", height.getText());
			json.addProperty("location", url);

			File target = new File(locField.getText());
			Utils.write(target, json.toString());

			setVisible(false);
			dispose();
		});
		gbc.gridy = 5;
		gbc.gridx = 0;
		panel.add(export, gbc);

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.gridx = 1;
		panel.add(cancel, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		panel.add(new JLabel("Loc"), gbc);

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.BOTH;

		locField.setPreferredSize(new Dimension(200, 25));
		filePanel.add(locField, gbc);

		JButton browse = new JButton("Browse");
		browse.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int choice = chooser.showDialog(app.getJFrame(), "Select file");
			if (choice == JFileChooser.APPROVE_OPTION) {
				locField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		gbc.gridx = 1;
		filePanel.add(browse, gbc);

		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(filePanel, gbc);

		locField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				export.setEnabled(locField.getText() != null);

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		setContentPane(panel);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

}
