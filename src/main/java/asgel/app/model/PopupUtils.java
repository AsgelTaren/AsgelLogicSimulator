package asgel.app.model;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import asgel.app.App;
import asgel.app.FileLookerDialog;
import asgel.core.model.Link;
import asgel.core.model.ModelOBJ;
import asgel.core.model.Pin;

/**
 * @author Florent Guille
 **/

public class PopupUtils {

	public static final JPopupMenu forModelOBJ(ModelHolder holder, ModelOBJ obj, App app) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem setName = new JMenuItem("Set Name");
		menu.add(setName);

		JCheckBoxMenuItem moveable = new JCheckBoxMenuItem("Moveable");
		moveable.setSelected(obj.isMoveable());
		moveable.addActionListener(e -> {
			obj.setMoveable(moveable.isSelected());
		});

		menu.add(moveable);

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

		JMenu custom = obj.getPopupMenu(app);
		if (custom != null) {
			menu.add(custom);
		}
		return menu;
	}

	public static final JPopupMenu forPin(ModelHolder holder, Pin p, App app) {
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

		JMenuItem removeLinkPath = new JMenuItem("Remove Link Path");
		removeLinkPath.setEnabled(p.getLink() != null);
		removeLinkPath.addActionListener(e -> {
			p.getLink().setPath(null);
		});
		menu.add(removeLinkPath);

		JMenuItem setColor = new JMenuItem("Set Link Color");
		setColor.setEnabled(p.getLink() != null);
		setColor.addActionListener(e -> {
			Color res = JColorChooser.showDialog(app.getJFrame(), "Select color", p.getLink().getColor());
			if (res != null) {
				p.getLink().setColor(res);
			}
		});
		menu.add(setColor);

		JMenuItem clearColor = new JMenuItem("Clear Link Color");
		clearColor.setEnabled(p.getLink() != null);
		clearColor.addActionListener(e -> p.getLink().setColor(null));
		menu.add(clearColor);

		return menu;
	}

	public static JPopupMenu forCat(ModelHolder holder, String cat, App app) {
		JPopupMenu res = new JPopupMenu();

		JMenuItem setIcon = new JMenuItem("Set Icon");
		setIcon.addActionListener(act -> {
			try {
				FileLookerDialog dialog = new FileLookerDialog(app.getJFrame(), app.getWorkingDir(),
						app.getText("iconselection"), f -> f.getName().toLowerCase().endsWith(".png"));
				dialog.setVisible(true);
				if (dialog.getResult() != null) {
					File target = dialog.getResult();
					if (target.toPath().startsWith(holder.getFile().getParentFile().toPath().toAbsolutePath())) {
						int choice = JOptionPane.showConfirmDialog(app.getJFrame(),
								"You can use this icons through an relative url. This allows you to make your file and icons portable. Do you want to proceed?",
								"WARNING", JOptionPane.YES_NO_OPTION);
						if (choice == JOptionPane.YES_OPTION) {
							String url = "relative:"
									+ holder.getFile().getParentFile().toPath().relativize(target.toPath()).toString();
							holder.getModel().getCatIcons().put(cat, url);
						} else {
							String url = "absolute:" + Path.of(app.getWorkingDir().getCanonicalPath())
									.relativize(target.toPath()).toString();
							holder.getModel().getCatIcons().put(cat, url);
						}
						app.getObjectsTree().repaint();
						System.out.println(holder.getModel().getCatIcons());
					} else {
						String url = "absolute:" + Path.of(app.getWorkingDir().getCanonicalPath())
								.relativize(target.toPath()).toString();
						holder.getModel().getCatIcons().put(cat, url);
					}
					holder.reloadCatIcons();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		res.add(setIcon);

		return res;
	}
}
