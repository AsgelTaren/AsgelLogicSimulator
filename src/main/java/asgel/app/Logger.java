package asgel.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Florent Guille
 **/

public class Logger {

	public static final Logger INSTANCE = new Logger("");

	private JFrame frame;
	private JTextArea area;
	private String prefix = "";

	private Logger(String prefix) {
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
		area.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(area);
		scroll.setPreferredSize(new Dimension(600, 800));
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scroll, gbc);

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

	private Logger(String prefix, JFrame frame, JTextArea area) {
		this.prefix = prefix;
		this.frame = frame;
		this.area = area;
	}

	public void log(String s) {
		area.append(prefix + " " + s + "\n");
	}

	public void setVisible(boolean vis) {
		frame.setVisible(vis);
	}

	public Logger derivateLogger(String prefix) {
		return new Logger("".equals(this.prefix) ? prefix : this.prefix + " " + prefix, frame, area);
	}

}
