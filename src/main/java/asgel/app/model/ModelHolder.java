package asgel.app.model;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
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
import asgel.core.model.Link;
import asgel.core.model.Model;
import asgel.core.model.ModelOBJ;
import asgel.core.model.ModelRegistry.ObjectEntry;
import asgel.core.model.Pin;

public class ModelHolder implements MouseMotionListener, MouseListener, MouseWheelListener {

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
	private Pin highPin;
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
		renderer.push();
		renderer.center();
		renderer.translate(-camx, -camy);
		renderer.scale(zoom);

		for (ModelOBJ obj : model.getObjects()) {
			obj.render(renderer, highOBJ, highPin);
		}

		for (Link l : model.getLinks()) {
			l.isCoherent();
		}

		renderer.pop();

		renderer.drawString("Camera: " + camx + ", " + camy, 15, 15, Color.BLACK);
		renderer.drawString("Zoom: " + zoom, 15, 30, Color.BLACK);
		if (highOBJ != null) {
			renderer.drawString(highOBJ.toString(), 15, 45, Color.BLACK);
		}

		if (mouseInModel != null) {
			renderer.drawString("Mouse: " + mouseInModel.x + ", " + mouseInModel.y, 15, 60, Color.BLACK);
			for (int i = 0; i < model.getObjects().size(); i++) {
				ModelOBJ obj = model.getObjects().get(i);
				renderer.drawString(
						obj + " : " + obj.getX() + ", " + obj.getY() + ": " + obj.fromModelToObject(mouseInModel), 15,
						30 + (i + 1) * 15, Color.BLACK);
			}
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
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

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
			if (highOBJ != null) {
				highOBJ.setPos(delta.add(mouseInModel));
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

}