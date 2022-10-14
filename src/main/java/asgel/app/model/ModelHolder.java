package asgel.app.model;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import asgel.app.App;
import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;
import asgel.core.model.Clickable;
import asgel.core.model.Link;
import asgel.core.model.Model;
import asgel.core.model.ModelOBJ;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.Pin;

public class ModelHolder implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {

	// OBJ Flavor
	public static final DataFlavor OBJFLAVOR = new DataFlavor(ObjectEntry.class, "Flavor for object registry");

	// Model reference
	private Model model;

	// Camera pos
	private float camx, camy, zoom = 1.0f;

	// App reference
	private App app;

	// Highlighted
	private ModelOBJ highOBJ;
	private Pin highPin, anchor;
	private Point delta;

	// Mouse
	private Point mouseInModel;

	// To Add
	private ArrayList<ModelOBJ> objToAdd;

	public ModelHolder(Model model, App app) {
		this.model = model;
		this.app = app;

		objToAdd = new ArrayList<ModelOBJ>();
	}

	public void render(Renderer renderer) {
		if (objToAdd.size() > 0) {
			model.getObjects().addAll(objToAdd);
			objToAdd = new ArrayList<ModelOBJ>();
		}
		if (mouseInModel == null)
			return;
		renderer.push();
		renderer.center();
		renderer.translate(-camx, -camy);
		renderer.scale(zoom);

		for (ModelOBJ obj : model.getObjects()) {
			obj.render(renderer, highOBJ, highPin, anchor);
		}

		for (Link l : model.getLinks()) {
			l.isCoherent();
			l.render(renderer);
		}

		if (anchor != null) {
			renderer.drawLine(anchor.posInModel(),
					(highPin != null && highPin.getLink() == null && highPin.isInput() != anchor.isInput()
							&& highPin.getSize() == anchor.getSize()) ? highPin.posInModel() : mouseInModel,
					Color.BLACK);
		}

		renderer.pop();

		renderer.drawString("Camera: " + camx + ", " + camy, 15, 15, Color.BLACK);
		renderer.drawString("Zoom: " + zoom, 15, 30, Color.BLACK);
		if (highOBJ != null) {
			renderer.drawString(highOBJ.toString(), 15, 60, Color.BLACK);
		}

		renderer.drawString("Mouse: " + mouseInModel.x + ", " + mouseInModel.y, 15, 45, Color.BLACK);
		for (int i = 0; i < model.getObjects().size(); i++) {
			ModelOBJ obj = model.getObjects().get(i);
			renderer.drawString(
					obj + " : " + obj.getX() + ", " + obj.getY() + ": " + obj.fromModelToObject(mouseInModel), 15,
					60 + (i + 1) * 15, Color.BLACK);
		}

	}

	public synchronized void addObject(ObjectEntry entry, int mousex, int mousey) {
		Point mouse = fromCameraToModel(new Point(mousex, mousey));
		ModelOBJ obj = entry.getProvider().apply(mouse);
		if (obj != null) {
			objToAdd.add(obj);
		}
	}

	public Point fromCameraToModel(Point p) {
		return new Point((p.x + camx - (app.getRenderer().getWidth() >> 1)) / zoom,
				(p.y + camy - (app.getRenderer().getHeight() >> 1)) / zoom);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		float oldzoom = zoom;
		zoom *= (float) Math.pow(1.15, e.getWheelRotation());
		camx = (app.getRenderer().getWidth() >> 1) - e.getX()
				+ zoom / oldzoom * (e.getX() + camx - (app.getRenderer().getWidth() >> 1));
		camy = (app.getRenderer().getHeight() >> 1) - e.getY()
				+ zoom / oldzoom * (e.getY() + camy - (app.getRenderer().getHeight() >> 1));
		refreshHigh();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (highOBJ != null) {
				delta = highOBJ.getPos().sub(mouseInModel);
			} else if (highPin != null && highPin.getLink() == null) {
				anchor = highPin;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (anchor != null && highPin != null && highPin.getLink() == null && highPin.getSize() == anchor.getSize()
				&& highPin.isInput() != anchor.isInput()) {
			model.getLinks().add(Link.createAndApply(anchor, highPin));
		}
		anchor = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseInModel = fromCameraToModel(new Point(e.getX(), e.getY()));
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (highOBJ != null && anchor == null) {
				highOBJ.setPos(delta.add(mouseInModel));
			} else if (anchor != null) {
				refreshHigh();
				if (highPin == anchor) {
					highPin = null;
				}
			}
		}
		if (SwingUtilities.isMiddleMouseButton(e)) {

		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseInModel = fromCameraToModel(new Point(e.getX(), e.getY()));
		refreshHigh();
	}

	private void refreshHigh() {
		highOBJ = null;
		highPin = null;

		for (ModelOBJ obj : model.getObjects()) {
			Point point = obj.fromModelToObject(mouseInModel);
			if (obj.containsFromObject(point, 5)) {
				for (Pin p : obj.getPins()) {
					if (p.getPos().distSQ(point) < 64) {
						highPin = p;
						return;
					}
				}
				if (obj.containsFromObject(point, 0)) {
					highOBJ = obj;
					return;
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_C) {
			if (highOBJ != null && highOBJ instanceof Clickable c) {
				c.onClick();
				ArrayList<ModelOBJ> ref = new ArrayList<ModelOBJ>();
				ref.add(highOBJ);
				model.refresh(ref);
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}