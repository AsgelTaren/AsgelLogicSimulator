package asgel.core.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import javax.swing.JMenu;
import javax.swing.JPanel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import asgel.app.App;
import asgel.core.gfx.Direction;
import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;
import asgel.core.model.BundleRegistry.ObjectEntry;

/**
 * @author Florent Guille
 **/

public abstract class ModelOBJ {

	// Pos and dim
	protected int x, y, width, height;
	protected Direction rot = Direction.EAST;
	protected boolean isMoveable = true;

	// Name
	protected String name, symbol;

	// Object registry
	protected ObjectEntry entry;

	// Pins
	protected Pin[] pins;

	private boolean showName, showAligns;

	// Category
	private String category;

	protected ModelOBJ(String name, String symbol, int x, int y, int width, int height, int pins) {
		this.name = name;
		this.symbol = symbol;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.pins = new Pin[pins];
	}

	public void placePins() {
		EnumMap<Direction, ArrayList<Pin>> map = new EnumMap<Direction, ArrayList<Pin>>(Direction.class);
		for (Direction d : Direction.values()) {
			map.put(d, new ArrayList<Pin>());
		}

		for (Pin p : pins) {
			map.get(p.getRotation()).add(p);
		}

		ArrayList<Pin> current = map.get(Direction.EAST);
		for (int i = 0; i < current.size(); i++) {
			Pin p = current.get(i);
			p.x = width;
			p.y = (int) ((i + 1) / (float) (current.size() + 1) * height);
		}
		current = map.get(Direction.SOUTH);
		for (int i = 0; i < current.size(); i++) {
			Pin p = current.get(i);
			p.y = height;
			p.x = (int) ((i + 1) / (float) (current.size() + 1) * width);
		}
		current = map.get(Direction.WEST);
		for (int i = 0; i < current.size(); i++) {
			Pin p = current.get(i);
			p.x = 0;
			p.y = (int) ((i + 1) / (float) (current.size() + 1) * height);
		}
		current = map.get(Direction.NORTH);
		for (int i = 0; i < current.size(); i++) {
			Pin p = current.get(i);
			p.y = 0;
			p.x = (int) ((i + 1) / (float) (current.size() + 1) * width);
		}
	}

	public abstract void update();

	public final JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("x", x);
		json.addProperty("y", y);
		json.addProperty("rot", rot.toString());
		json.addProperty("entry", entry.getFullID());
		json.addProperty("moveable", isMoveable);
		json.addProperty("name", name);
		if (category != null)
			json.addProperty("cat", category);
		toJsonInternal(json);
		return json;
	}

	protected void loadPins(JsonObject json) {
		JsonArray data = json.get("pinsData").getAsJsonArray();
		for (int i = 0; i < data.size(); i++) {
			pins[i].setData(data.get(i).getAsJsonArray());
		}
	}

	protected void toJsonInternal(JsonObject obj) {

	}

	public void render(Renderer renderer, ModelOBJ highOBJ, Pin highPin, Pin anchor) {
		renderer.push();
		renderer.translate(x, y);
		renderer.applyRot(rot, new Point(width >> 1, height >> 1));

		if (showName) {
			renderer.drawString(name, width + 10, height + 10, Color.BLACK);
		}
		if (showAligns) {
			renderer.drawLine(-1000, 0, 1000, 0, Color.BLACK);
			renderer.drawLine(-1000, height, 1000, height, Color.BLACK);
			renderer.drawLine(0, -1000, 0, 1000, Color.BLACK);
			renderer.drawLine(width, -1000, width, 1000, Color.BLACK);
		}
		if (entry.getBackground() != null) {
			renderer.drawImage(entry.getBackground(), 0, 0, width, height);
			if (highOBJ == this) {
				renderer.drawRoundedRect(0, 0, width, height, 20, Color.RED);
			}
		} else {
			renderer.fillRoundedRect(0, 0, width, height, 20, Color.GRAY);
			renderer.drawRoundedRect(0, 0, width, height, 20, highOBJ == this ? Color.GREEN : Color.BLACK);
			renderer.drawCenteredString(symbol, width >> 1, height >> 1, Color.BLACK);

		}
		for (Pin p : pins) {
			p.render(renderer, highPin, anchor);

			if (highOBJ == this || highPin == p) {
				Point loc = p.getRotation().asVec(35).add(p.getPos());
				renderer.drawCenteredString(p.toString(), (int) loc.x, (int) loc.y,
						highPin == p ? Color.red : Color.BLACK);
			}
		}
		renderer.pop();
	}

	public JsonArray pinsToJson() {
		JsonArray res = new JsonArray(pins.length);
		for (Pin p : pins) {
			res.add(p.toJson());
		}
		return res;
	}

	public Point fromObjectToModel(Point p) {
		return rot.applyTo(p, new Point(width >> 1, height >> 1)).add(getPos());
	}

	public Point fromModelToObject(Point p) {
		return rot.opposite().applyTo(p.sub(getPos()), new Point(width >> 1, height >> 1));
	}

	public Pin matchPin(Point p, int distSQ) {
		for (Pin pin : pins) {
			if (pin.posInModel().distSQ(p) < distSQ) {
				return pin;
			}
		}
		return null;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Point getPos() {
		return new Point(x, y);
	}

	public void setPos(Point p) {
		this.x = (int) p.x;
		this.y = (int) p.y;
	}

	public boolean isMoveable() {
		return isMoveable;
	}

	public void setMoveable(boolean m) {
		this.isMoveable = m;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ObjectEntry getEntry() {
		return entry;
	}

	public Direction getRotation() {
		return rot;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setRotation(Direction rot) {
		this.rot = rot;
	}

	ModelOBJ setEntry(ObjectEntry entry) {
		this.entry = entry;
		return this;
	}

	public Pin[] getPins() {
		return pins;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean showName() {
		return showName;
	}

	public boolean showAligns() {
		return showAligns;
	}

	public void setShowName(boolean showName) {
		this.showName = showName;
	}

	public void setShowAligns(boolean showAligns) {
		this.showAligns = showAligns;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean containsFromObject(Point p, int margin) {
		return -margin <= p.x && -margin <= p.y && margin + width > p.x && margin + height >= p.y;
	}

	public boolean containsFromModel(Point p, int margin) {
		return containsFromObject(fromModelToObject(p), margin);
	}

	public JMenu getPopupMenu(App app) {
		return null;
	}

	public JPanel[] getDetailsPanels() {
		return new JPanel[0];
	}

	public int indexOfPin(Pin p) {
		for (int i = 0; i < pins.length; i++) {
			if (p == pins[i]) {
				return i;
			}
		}
		return -1;
	}

	public static final JsonArray toJsonArray(boolean[] data) {
		JsonArray result = new JsonArray(data.length);
		for (int i = 0; i < data.length; i++) {
			result.add(data[i]);
		}
		return result;
	}

	public static final boolean[] toBooleanArray(JsonArray arr) {
		boolean[] result = new boolean[arr.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = arr.get(i).getAsBoolean();
		}
		return result;
	}

	public static boolean[] cloneArray(boolean[] input) {
		return Arrays.copyOf(input, input.length);
	}

	public void updateMousePos(Point p) {

	}
}