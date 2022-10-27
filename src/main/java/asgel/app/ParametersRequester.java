package asgel.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import asgel.core.model.IParametersRequester;

public class ParametersRequester implements IParametersRequester {

	private App app;

	public ParametersRequester(App app) {
		super();
		this.app = app;
	}

	@Override
	public String[] getParameters(String... args) {
		ParReqDialog dia = new ParReqDialog(app.getJFrame(), args);
		dia.setVisible(true);
		return dia.getChoice();
	}

	@Override
	public int[] getParametersAsInt(String... args) {
		String[] temp = getParameters(args);
		if (temp == null) {
			return null;
		}
		int[] res = new int[temp.length];
		try {
			for (int i = 0; i < res.length; i++) {
				res[i] = Integer.parseInt(temp[i]);
			}
			return res;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@SuppressWarnings("serial")
	private class ParReqDialog extends JDialog {

		private JLabel[] labels;
		private JTextField[] fields;
		private JButton ok, cancel;

		private ParReqDialog(JFrame frame, String[] args) {
			super(frame, "Parameters", true);
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = gbc.gridheight = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			setLocation(frame.getWidth() >> 1, frame.getHeight() >> 1);

			labels = new JLabel[args.length];
			fields = new JTextField[args.length];
			for (int i = 0; i < args.length; i++) {
				labels[i] = new JLabel(args[i]);
				gbc.gridx = 0;
				gbc.gridy = i;
				panel.add(labels[i], gbc);

				fields[i] = new JTextField();
				fields[i].setPreferredSize(new Dimension(200, 25));
				gbc.gridx = 1;
				panel.add(fields[i], gbc);
			}
			gbc.gridx = 0;
			gbc.gridy = args.length;
			ok = new JButton("Ok");
			ok.addActionListener(e -> {
				dispose();
				setVisible(false);
			});
			panel.add(ok, gbc);

			gbc.gridx = 1;
			cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				for (JTextField field : fields) {
					field.setText(null);
				}
				dispose();
				setVisible(false);
			});
			panel.add(cancel, gbc);

			setContentPane(panel);
			pack();

			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					for (JTextField field : fields) {
						field.setText(null);
					}
				}
			});
		}

		public String[] getChoice() {
			String[] res = new String[labels.length];
			for (int i = 0; i < res.length; i++) {
				String s = fields[i].getText();
				if (s == null || s.equals("")) {
					return null;
				}
				res[i] = s;
			}
			return res;
		}

	}

	@Override
	public File getFile(File startDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(startDir);
		int choice = chooser.showOpenDialog(app.getJFrame());
		if (choice == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

}
