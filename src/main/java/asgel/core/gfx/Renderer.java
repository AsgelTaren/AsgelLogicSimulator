package asgel.core.gfx;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Stack;

@SuppressWarnings("serial")
public class Renderer extends Canvas {

	// Graphical objects
	private BufferStrategy bs;
	private Graphics2D g;

	// Font
	private Font font = new Font("Manrope", Font.PLAIN, 17);;

	// Bezier
	private int bezier_complex = 15;

	// Transforms
	private Stack<AffineTransform> stack;

	public Renderer(int bezier_complex) {
		super();

		this.bezier_complex = bezier_complex;
	}

	public void create() {
		createBufferStrategy(3);
	}

	/**
	 * This method starts the preparing-drawing-ending process, and must be called
	 * before any drawing methods. To finish a drawing iteration,the {@link #end()}
	 * method must be called as well at the end
	 **/
	public boolean begin() {
		bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return false;
		}
		g = (Graphics2D) bs.getDrawGraphics();
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setFont(font);
		stack = new Stack<AffineTransform>();
		return true;
	}

	/** This method release every graphical resources held by the system **/
	public void end() {
		bs.show();
		g.dispose();
	}

	public void translate(float x, float y) {
		g.translate(x, y);
	}

	public void scale(float s) {
		g.scale(s, s);
	}

	public void scale(float sx, float sy) {
		g.scale(sx, sy);
	}

	public void push() {
		stack.push(g.getTransform());
	}

	public void pop() {
		g.setTransform(stack.pop());
	}

	public void applyRot(Direction rot, Point center) {
		g.translate(center.x, center.y);
		AffineTransform t = g.getTransform();
		t.quadrantRotate(rot.ordinal());
		g.setTransform(t);
		g.translate(-center.x, -center.y);
	}

	public void center() {
		g.translate(getWidth() >> 1, getHeight() >> 1);
	}

	//// Drawing methods

	public void drawRect(int x, int y, int width, int height, Color c) {
		g.setColor(c);
		g.drawRect(x, y, width, height);
	}

	public void fillRect(int x, int y, int width, int height, Color c) {
		g.setColor(c);
		g.fillRect(x, y, width, height);
	}

	public void fillRoundedRect(int x, int y, int width, int height, int arc, Color c) {
		g.setColor(c);
		g.fillRoundRect(x, y, width, height, arc, arc);
	}

	public void drawRoundedRect(int x, int y, int width, int height, int arc, Color c) {
		g.setColor(c);
		g.drawRoundRect(x, y, width, height, arc, arc);
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color c) {
		g.setColor(c);
		g.drawLine(x1, y1, x2, y2);
	}

	public void drawLine(Point p1, Point p2, Color c) {
		drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y, c);
	}

	public void fillOval(int x, int y, int width, Color c) {
		g.setColor(c);
		g.fillOval(x, y, width, width);
	}

	public void drawOval(int x, int y, int width, Color c) {
		g.setColor(c);
		g.drawOval(x, y, width, width);
	}

	public void drawBezierCurve(Point p0, Point p1, Point p2, Point p3, Color col) {
		g.setColor(col);
		Point last = p0;
		for (int i = 1; i <= bezier_complex; i++) {
			float t = i / (float) bezier_complex;
			float difft = 1.0f - t;
			float a = difft * difft * difft;
			float b = 3.0f * t * difft * difft;
			float c = 3.0f * t * t * difft;
			float d = t * t * t;
			Point p = new Point(p0.x * a + p1.x * b + p2.x * c + p3.x * d, p0.y * a + p1.y * b + p2.y * c + p3.y * d);
			g.drawLine((int) last.x, (int) last.y, (int) p.x, (int) p.y);
			last = p;
		}
	}

	public void drawCenteredString(String s, int x, int y, Color c) {
		g.setColor(c);
		FontMetrics fm = g.getFontMetrics();
		int b = y + ((fm.getAscent() - fm.getDescent()) >> 1);
		g.drawString(s, x - (fm.stringWidth(s) >> 1), b);
	}

	public void drawString(String s, int x, int y, Color c) {
		g.setColor(c);
		g.drawString(s, x, y);
	}

	public void drawImage(BufferedImage img, int x, int y, int width, int height) {
		g.drawImage(img, x, y, width, height, null);
	}

	// Getters

	public Graphics2D getGraphics2D() {
		return g;
	}

	public AffineTransform getTransform() {
		return g.getTransform();
	}

	public Stack<AffineTransform> getTransformStack() {
		return stack;
	}

	public Font getFont() {
		return font;
	}

	// Setters

	public void setFont(Font font) {
		this.font = font;
	}

	public void setStroke(int s) {
		g.setStroke(new BasicStroke(s));
	}
}