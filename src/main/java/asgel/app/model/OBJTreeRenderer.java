package asgel.app.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.ModelTab;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class OBJTreeRenderer extends DefaultTreeCellRenderer {

	private static Font tabFont = new Font("Manrope", Font.BOLD, 14), defFont = new Font("Manrope", Font.PLAIN, 12);

	private ArrayList<ObjectEntry> searchResult;

	public OBJTreeRenderer() {
		searchResult = new ArrayList<ObjectEntry>();
	}

	public void setSearchResult(ArrayList<ObjectEntry> searchResult) {
		this.searchResult = searchResult;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		JLabel res = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		res.setFont(defFont);
		res.setText(value.toString());
		res.setForeground(null);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object data = node.getUserObject();
		if (data instanceof ModelTab tab) {
			res.setToolTipText(tab.getFullID());
			res.setIcon(tab.getIcon());
			res.setFont(tabFont);
			res.setForeground(tab.getColor());
		} else if (data instanceof ObjectEntry entry) {
			res.setToolTipText(entry.getFullID());
			if (searchResult.contains(entry)) {
				res.setForeground(Color.GREEN);
			}
			res.setIcon(entry.getTabInstance().getIcon());

		}
		return res;
	}

}
