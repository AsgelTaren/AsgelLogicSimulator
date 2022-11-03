package asgel.app.model;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import asgel.core.gfx.Direction;
import asgel.core.model.ModelOBJ;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class OBJDetailsDialog extends JDialog {

	public OBJDetailsDialog(JFrame frame, ModelOBJ obj) {
		super(frame, "Details", true);

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("General", new GeneralPanel(obj));
		for (JPanel panel : obj.getDetailsPanels()) {
			tabs.add(panel.toString(), panel);
		}
		setContentPane(tabs);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
	}

	public void showDialog() {
		setVisible(true);
	}

	@SuppressWarnings("unused")
	private class GeneralPanel extends JPanel {

		private ModelOBJ obj;

		public GeneralPanel(ModelOBJ obj) {
			super();
			this.obj = obj;

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			// Position and rotation

			NumberFormat format = NumberFormat.getInstance();
			NumberFormatter formatter = new NumberFormatter(format);
			formatter.setValueClass(Integer.class);
			formatter.setAllowsInvalid(false);

			JPanel pos = new JPanel();
			pos.setBorder(BorderFactory.createTitledBorder("Position and rotation"));
			pos.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridheight = gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);

			JLabel xlab = new JLabel("x");
			pos.add(xlab, gbc);

			JFormattedTextField xfield = new JFormattedTextField(formatter);
			xfield.setPreferredSize(new Dimension(150, 25));
			xfield.setText(obj.getX() + "");
			xfield.setEnabled(obj.isMoveable());
			xfield.addActionListener(e -> {
				obj.setX(Integer.parseInt(xfield.getText()));
			});
			gbc.gridx = 1;
			pos.add(xfield, gbc);

			JLabel ylab = new JLabel("y");
			gbc.gridx = 2;
			pos.add(ylab, gbc);

			JFormattedTextField yfield = new JFormattedTextField(formatter);
			yfield.setPreferredSize(new Dimension(150, 25));
			yfield.setText(obj.getY() + "");
			yfield.addActionListener(e -> {
				obj.setY(Integer.parseInt(yfield.getText()));
			});
			yfield.setEnabled(obj.isMoveable());
			gbc.gridx = 3;
			pos.add(yfield, gbc);

			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;

			JLabel rotlab = new JLabel("Rotation");
			pos.add(rotlab, gbc);

			JComboBox<Direction> rotcomb = new JComboBox<>(Direction.values());
			rotcomb.setSelectedItem(obj.getRotation());
			rotcomb.addActionListener(e -> obj.setRotation((Direction) rotcomb.getSelectedItem()));
			rotcomb.setEnabled(obj.isMoveable());
			gbc.gridx = 2;
			pos.add(rotcomb, gbc);

			add(pos);

			JPanel desc = new JPanel();
			desc.setBorder(BorderFactory.createTitledBorder("Name and others"));
			desc.setLayout(new GridBagLayout());
			gbc.gridheight = gbc.gridwidth = 1;
			gbc.gridx = gbc.gridy = 0;
			desc.add(new JLabel("Name"), gbc);

			JTextField namefield = new JTextField(obj.toString());
			namefield.setPreferredSize(new Dimension(150, 25));
			namefield.addActionListener(e -> {
				obj.setName(namefield.getText());
			});
			gbc.gridx = 1;
			desc.add(namefield, gbc);

			add(desc);
		}

	}

}
