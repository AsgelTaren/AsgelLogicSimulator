package asgel.core.gfx;

public enum Direction {

	EAST, SOUTH, WEST, NORTH;

	private Direction() {

	}

	public Direction next() {
		return values()[(ordinal() + 1) & 3];
	}

	public Direction opposite() {
		switch (this) {
		case SOUTH:
			return NORTH;
		case NORTH:
			return SOUTH;
		default:
			return this;
		}
	}

	public Direction getInverse() {
		switch (this) {
		case SOUTH:
			return NORTH;
		case NORTH:
			return SOUTH;
		case EAST:
			return WEST;
		case WEST:
			return EAST;
		}
		return null;
	}

	public Direction applyTo(Direction rot) {
		return values()[(ordinal() + rot.ordinal()) & 3];
	}

	public Point applyTo(Point p) {
		switch (this) {
		case EAST:
			return new Point(p);
		case SOUTH:
			return new Point(-p.y, p.x);
		case WEST:
			return new Point(-p.x, -p.y);
		case NORTH:
			return new Point(p.y, -p.x);
		}
		return null;
	}

	public Point applyTo(Point p, Point center) {
		return applyTo(p.sub(center)).add(center);
	}

	public Point asVec(int dist) {
		return applyTo(new Point(dist, 0));
	}

}
