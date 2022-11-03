package asgel.core.gfx;

import java.util.function.Function;

import com.google.gson.JsonObject;

/**
 * @author Florent Guille
 **/

public class Point {

	public float x, y;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point(JsonObject json) {
		this.x = json.get("x").getAsFloat();
		this.y = json.get("y").getAsFloat();
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public Point add(Point p) {
		return new Point(this.x + p.x, this.y + p.y);
	}

	public Point sub(Point p) {
		return new Point(this.x - p.x, this.y - p.y);
	}

	public Point scale(float s) {
		return new Point(this.x * x, this.y * s);
	}

	public float normSQ() {
		return x * x + y * y;
	}

	public float norm() {
		return (float) Math.sqrt(normSQ());
	}

	public float dist(Point p) {
		return p.sub(this).norm();
	}

	public float distSQ(Point p) {
		return p.sub(this).normSQ();
	}

	public Point schurProduct(Point p) {
		return new Point(this.x * p.x, this.y * p.y);
	}

	public Point map(Function<Float, Float> func) {
		return new Point(func.apply(this.x), func.apply(this.y));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point p) {
			return x == p.x && y == p.y;
		}
		return false;
	}

	@Override
	public String toString() {
		return "{x:" + x + ",y:" + y + "}";
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("x", x);
		json.addProperty("y", y);
		return json;
	}
}