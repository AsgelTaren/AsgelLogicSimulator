package asgel.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Florent Guille
 **/

public class Logger {

	public static final Logger INSTANCE = new Logger();

	private JFrame frame;
	private JTextArea area;

	private Logger() {
		frame = new JFrame("LOGS");
		frame.setIconImage(App.ICON);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("LOGS"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;

		area = new JTextArea();
		area.setPreferredSize(new Dimension(600, 800));
		panel.add(area, gbc);

		JButton clear = new JButton("Clear Logs");
		clear.addActionListener(e -> {
			area.setText("");
		});
		gbc.gridy = 1;
		panel.add(clear, gbc);

		frame.setContentPane(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setLocationRelativeTo(null);

	}

	public void log(String s) {
		area.append(s + "\n");
	}

	public void setVisible(boolean vis) {
		frame.setVisible(vis);
	}

}
