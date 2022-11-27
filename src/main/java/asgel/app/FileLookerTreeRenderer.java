package asgel.app;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import asgel.app.FileLookerDialog.FileHolder;

@SuppressWarnings("serial")
public class FileLookerTreeRenderer extends DefaultTreeCellRenderer {

	public FileLookerTreeRenderer() {
		super();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		FileHolder holder = (FileHolder) ((DefaultMutableTreeNode) value).getUserObject();
		File file = null;
		JLabel res = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		try {
			file = new File(holder.f.getCanonicalPath() + ".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (file.exists()) {
			try {
				ImageIcon icon = Utils.loadIcon(new FileInputStream(file), 16);
				res.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

}
