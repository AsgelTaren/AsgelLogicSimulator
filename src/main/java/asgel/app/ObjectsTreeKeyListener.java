package asgel.app;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import asgel.app.model.ModelHolder;

public class ObjectsTreeKeyListener implements KeyListener {

	private App app;

	public ObjectsTreeKeyListener(App app) {
		this.app = app;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F5) {
			ModelHolder holder = app.getSelectedModelHolder();
			if (holder != null) {
				holder.rebuildNodeRepresentation();
				holder.reloadCatIcons();
				app.updateObjectsTree();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
