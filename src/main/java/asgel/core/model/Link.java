package asgel.core.model;

import java.awt.Color;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;

public class Link {

	private Pin start, end;
	private ArrayList<Point> path;
	private Color c;

	private Link(Pin start, Pin end) {
		this.start = start;
		this.end = end;
	}

	public void render(Renderer renderer) {
		if (path != null) {
			for (int i = 0; i < path.size() - 1; i++) {
				renderer.drawLine(path.get(i), path.get(i + 1),
						c != null ? c : start.getSize() == 1 && start.getData()[0] ? Color.RED : Color.BLACK);
			}
		} else
			renderer.drawBezierCurve(start.posInModel(), start.posInModel().add(start.getRealRotation().asVec(100)),
					end.posInModel().add(end.getRealRotation().asVec(100)), end.posInModel(),
					c != null ? c : start.getSize() == 1 && start.getData()[0] ? Color.RED : Color.BLACK);
	}

	public static Link create(Pin a, Pin b) {
		if (a.isInput() != b.isInput() && a.getLink() == null && b.getLink() == null && a.getSize() == b.getSize()) {
			return a.isInput() ? new Link(b, a) : new Link(a, b);
		}
		return null;
	}

	public void setPath(ArrayList<Point> path) {
		this.path = path;
	}

	public ArrayList<Point> getPath() {
		return path;
	}

	public void setPathFromJson(JsonArray arr) {
		path = new ArrayList<Point>(arr.size());
		for (JsonElement e : arr) {
			path.add(new Point(e.getAsJsonObject()));
		}
	}

	public static Link createAndApply(Pin a, Pin b) {
		if (a.isInput() != b.isInput() && a.getLink() == null && b.getLink() == null && a.getSize() == b.getSize()) {
			Link res = a.isInput() ? new Link(b, a) : new Link(a, b);
			a.setLink(res);
			b.setLink(res);
			return res;
		}
		return null;
	}

	public void transmitData() {
		end.setData(start.getData());
	}

	public boolean isCoherent() {
		for (int i = 0; i < start.getSize(); i++) {
			if (start.getData()[i] != end.getData()[i])
				return false;
		}
		return true;
	}

	public Pin getOther(Pin p) {
		return p == start ? end : start;
	}

	public Pin getStart() {
		return start;
	}

	public Pin getEnd() {
		return end;
	}

	public Color getColor() {
		return c;
	}

	public void setColor(Color c) {
		this.c = c;
	}

}