package asgel.core.model;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * @author Florent Guille
 **/

public class ModelTab {

	private String name, id;
	private ImageIcon icon;
	private Color color;
	private BundleRegistry registry;

	public ModelTab(String id) {
		this.id = id;
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

	public void setName(String name) {
		this.name = name;
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