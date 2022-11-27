package asgel.app;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import asgel.app.model.ModelHolder;
import asgel.core.model.ModelOBJ;

@SuppressWarnings("serial")
public class ObjectTreeRenderer extends DefaultTreeCellRenderer {

	private App app;

	public ObjectTreeRenderer(App app) {
		super();
		this.app = app;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		JLabel res = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		Object target = ((DefaultMutableTreeNode) value).getUserObject();
		if (target instanceof ModelOBJ obj) {
			res.setIcon(obj.getEntry().getTabInstance().getIcon());
		}
		if (target instanceof ObjectCategory cat) {
			ModelHolder holder = app.getSelectedModelHolder();
			if (holder != null) {
				String iconID = holder.getModel().getCatIcons().get(cat.toString());
				ImageIcon icon = holder.getCatIconsInstances().get(iconID);
				if (icon != null) {
					res.setIcon(icon);
				}
			}
		}
		return res;
	}

}
