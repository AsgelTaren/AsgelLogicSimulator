package asgel.app;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class LoadingFrame extends JDialog {

	private JTabbedPane tabs;
	private JPanel[] panels;
	private JButton launch;
	private App app;

	public LoadingFrame(App app) {
		super((Frame) null, app.getText("loading.title"), true);
		setIconImage(App.ICON);
		this.app = app;
	}

	public void build(JPanel... panels) {
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

		launch = new JButton(app.getText("loading.launch"));
		launch.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.gridwidth = 1;
		gbc.gridy = 1;
		panel.add(launch, gbc);

		JButton cancel = new JButton(app.getText("loading.cancel"));
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

	public JButton getLaunchButton() {
		return launch;
	}

	public App getApp() {
		return app;
	}

}
