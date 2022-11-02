package asgel.core.model;

import java.awt.Color;

import javax.swing.ImageIcon;

public class ModelTab {

	private String name, id;
	private ImageIcon icon;
	private Color color;
	private BundleRegistry registry;

	public ModelTab(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public ModelTab setIcon(ImageIcon icon) {
		this.icon = icon;
		return this;
	}

	public ModelTab setRegistry(BundleRegistry registry) {
		this.registry = registry;
		return this;
	}

	public ModelTab setColor(Color c) {
		this.color = c;
		return this;
	}

	public BundleRegistry getRegistry() {
		return registry;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return id;
	}

	public String getFullID() {
		return registry.getBundleID() + ":" + id;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return name;
	}

}