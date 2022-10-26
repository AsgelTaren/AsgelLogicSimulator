package asgel.app;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class LoadingFrame extends JDialog {

	private JTabbedPane tabs;
	private JPanel[] panels;

	public LoadingFrame(JPanel... panels) {
		super((Frame) null, "Loading", true);

		this.panels = panels;
		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		tabs = new JTabbedPane();
		for (JPanel pan : panels) {
			tabs.add(pan.toString(), pan);
		}
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		panel.add(tabs, gbc);

		JButton launch = new JButton("Launch");
		launch.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.gridwidth = 1;
		gbc.gridy = 1;
		panel.add(launch, gbc);

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> System.exit(0));
		gbc.gridx = 1;
		panel.add(cancel, gbc);

		setContentPane(panel);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	public JPanel[] getPanels() {
		return panels;
	}

	public void showDialog() {
		setVisible(true);
	}

}
