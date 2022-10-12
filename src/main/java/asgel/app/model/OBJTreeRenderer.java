package asgel.app.model;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import asgel.core.model.ModelRegistry.ObjectEntry;
import asgel.core.model.ModelTab;

@SuppressWarnings("serial")
public class OBJTreeRenderer extends DefaultTreeCellRenderer {

	private Font tabFont, defFont;

	public OBJTreeRenderer() {
		tabFont = new Font("Manrope", Font.BOLD, 14);
		defFont = new Font("Manrope", Font.PLAIN, 12);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		JLabel res = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		res.setFont(defFont);
		res.setText(value.toString());
		Object data = ((DefaultMutableTreeNode) value).getUserObject();
		if (data instanceof ModelTab tab) {
			res.setToolTipText(tab.getID() + ":" + tab.getName());
			res.setIcon(tab.getIcon());
			res.setFont(tabFont);
		} else if (data instanceof ObjectEntry entry) {
			res.setToolTipText(entry.getBundle() + ":" + entry.getId());
		}
		return res;
	}

}
