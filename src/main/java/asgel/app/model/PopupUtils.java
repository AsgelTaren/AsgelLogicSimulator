package asgel.app.model;

import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import asgel.core.model.Link;
import asgel.core.model.ModelOBJ;
import asgel.core.model.Pin;

public class PopupUtils {

	public static final JPopupMenu forModelOBJ(ModelHolder holder, ModelOBJ obj) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem setName = new JMenuItem("Set Name");
		menu.add(setName);

		JMenuItem showDetails = new JMenuItem("Show Details");
		showDetails.addActionListener(e -> {
			OBJDetailsDialog p = new OBJDetailsDialog(holder.getApp().getJFrame(), obj);
			p.showDialog();
		});
		menu.add(showDetails);

		JMenuItem remove = new JMenuItem("Remove");
		remove.addActionListener(e -> {
			ArrayList<ModelOBJ> list = new ArrayList<>();
			for (Pin p : obj.getPins()) {
				if (p.getLink() != null) {
					Link l = p.getLink();
					l.getOther(p).setLink(null);
					p.setLink(null);
					if (!p.isInput()) {
						l.getOther(p).clearData();
						list.add(l.getOther(p).getModelOBJ());
					}
					holder.getModel().getLinks().remove(l);
				}
			}
			holder.getModel().getObjects().remove(obj);
			holder.getModel().refresh(list);
		});
		menu.add(remove);

		JMenu custom = obj.getPopupMenu();
		if (custom != null) {
			menu.add(custom);
		}
		return menu;
	}

	public static final JPopupMenu forPin(ModelHolder holder, Pin p) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem remove = new JMenuItem("Remove Link");
		remove.setEnabled(p.getLink() != null);
		remove.addActionListener(e -> {
			Link l = p.getLink();
			l.getOther(p).setLink(null);
			ArrayList<ModelOBJ> list = new ArrayList<>();
			list.add(p.isInput() ? p.getModelOBJ() : l.getOther(p).getModelOBJ());
			if (p.isInput()) {
				p.clearData();
			} else {
				l.getOther(p).clearData();
			}
			holder.getModel().refresh(list);
			holder.getModel().getLinks().remove(l);
			p.setLink(null);
		});
		menu.add(remove);

		return menu;
	}

}
