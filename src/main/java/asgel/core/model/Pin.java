package asgel.core.model;

import java.awt.Color;

import com.google.gson.JsonArray;

import asgel.core.gfx.Direction;
import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;

/**
 * @author Florent Guille
 **/

public class Pin {

	// Data stored in the pin
	private boolean[] data;

	private int size;

	// Object
	private ModelOBJ object;

	// Pos
	int x, y;
	private Direction rot = Direction.EAST;

	// Name
	private String name;

	// Is Input
	private boolean isInput;

	// Link
	private Link link;

	// Sensibility
	private boolean isSensible = true;

	public Pin(ModelOBJ object, Direction rot, int size, String name, boolean isInput) {
		this.object = object;
		this.rot = rot;
		this.size = size;
		this.name = name;
		this.isInput = isInput;

		data = new boolean[size];
	}

	public void render(Renderer renderer, Pin highPin, Pin anchor) {
		if (size > 1) {
			renderer.fillRect(x - 5, y - 5, 10, 10,
					highPin == this ? Color.RED : (anchor == this ? Color.BLUE : Color.LIGHT_GRAY));
		} else if (size == 1) {
			renderer.fillOval(x - 5, y - 5, 10, (data[0]) ? Color.RED : Color.GREEN);
			renderer.fillOval(x - 3, y - 3, 6,
					highPin == this ? Color.RED : (anchor == this ? Color.BLUE : Color.LIGHT_GRAY));
		}
	}

	public JsonArray toJson() {
		return ModelOBJ.toJsonArray(data);
	}

	public Point posInModel() {
		return object.fromObjectToModel(new Point(x, y));
	}

	public Point getPos() {
		return new Point(x, y);
	}

	public Point fromPinToModelDir(int scale, int dist) {
		return rot.asVec(dist).scale(scale);
	}

	public boolean isInput() {
		return isInput;
	}

	public ModelOBJ getModelOBJ() {
		return object;
	}

	public int getSize() {
		return size;
	}

	public boolean[] getData() {
		return data;
	}

	public void setData(boolean[] data) {
		for (int i = 0; i < data.length; i++)
			this.data[i] = data[i];
	}

	public void setData(JsonArray arr) {
		for (int i = 0; i < size; i++) {
			data[i] = arr.get(i).getAsBoolean();
		}
	}

	public void clearData() {
		this.data = new boolean[size];
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isSensible() {
		return isSensible;
	}

	public Pin setIsSensible(boolean val) {
		this.isSensible = val;
		return this;
	}

	public Direction getRotation() {
		return rot;
	}

	public Direction getRealRotation() {
		return rot.applyTo(object.rot);
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	@Override
	public String toString() {
		return name;
	}

}