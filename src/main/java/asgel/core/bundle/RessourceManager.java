package asgel.core.bundle;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import asgel.app.Utils;
import asgel.core.model.GlobalRegistry;

public class RessourceManager {

	private ClassLoader loader;
	private GlobalRegistry regis;

	public RessourceManager(Class<?> c, GlobalRegistry regis) {
		loader = c.getClassLoader();
		this.regis = regis;
	}

	public InputStream resolveRessource(String loc) {
		return loader.getResourceAsStream(loc);
	}

	public BufferedImage resolveImage(String loc) {
		return Utils.loadImage(loader.getResourceAsStream(loc));
	}

	public GlobalRegistry getGlobalRegistry() {
		return regis;
	}

}
