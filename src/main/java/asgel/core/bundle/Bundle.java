package asgel.core.bundle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.ImageIcon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import asgel.app.Logger;
import asgel.app.Utils;
import asgel.core.model.BundleRegistry;
import asgel.core.model.GlobalRegistry;
import asgel.core.model.IParametersRequester;

public class Bundle {

	// File
	private File file;

	// Bundle's informations
	protected String name, id, desc;

	// Bundle's icon
	protected ImageIcon icon;

	// Registry
	protected BundleRegistry registry;
	protected GlobalRegistry global;

	// Parameters Requester
	protected IParametersRequester requester;

	// Class Loader
	private ClassLoader loader;

	public Bundle(File f, String id, GlobalRegistry global, ClassLoader loader, IParametersRequester requester) {
		this.file = f;
		this.id = id;
		this.global = global;
		this.loader = loader;
		this.requester = requester;
		this.registry = new BundleRegistry(id);
	}

	public void loadDetails(Logger logger) throws Exception {
		logger.log("Loading details for bundle with id: " + id + " ...");
		JsonObject obj = JsonParser.parseString(new String(resolveBundleResource("bundle.json").readAllBytes()))
				.getAsJsonObject();
		this.name = obj.get("name").getAsString();
		this.desc = obj.get("desc").getAsString();

		logger.log("Loaded details for bundle with id: " + id + " ! Attempting to load bundle's icon");
		try {
			icon = Utils.loadIcon(resolveBundleResource("logo.png"), 16);
		} catch (Exception e) {
			logger.log("Failed to load icon for bundle with id: " + id);
			logger.log(e.getMessage());
		}
	}

	public void onLoad() {

	}

	public void loadTextAtlas(Language lang, Logger log) {
		try {
			registry.ATLAS.loadDataFrom(resolveBundleResource(lang.getSymbol() + ".lang"));
		} catch (Exception e) {
			log.log("Failed to load lang file for bundle with id: " + id + ". Reason : " + e.getMessage());
		}
	}

	public static final Bundle preLoadBundle(File file, String id, String main, GlobalRegistry global,
			IParametersRequester requester) throws Exception {
		URLClassLoader child = new URLClassLoader(new URL[] { file.toURI().toURL() }, Bundle.class.getClassLoader());
		Class<?> target = Class.forName(main + ".Bundle", true, child);
		return (Bundle) target.getConstructor(File.class, String.class, GlobalRegistry.class, ClassLoader.class,
				IParametersRequester.class).newInstance(file, id, global, child, requester);
	}

	public InputStream resolveFileResource(String name) {
		return loader.getResourceAsStream(name);
	}

	public InputStream resolveBundleResource(String name) {
		return loader.getResourceAsStream("assets/" + id + "/" + name);
	}

	public BufferedImage resolveBundleImage(String name) {
		return Utils.loadImage(resolveBundleResource(name));
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return id;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public File getFile() {
		return file;
	}

	public BundleRegistry getBundleRegistry() {
		return registry;
	}

	public GlobalRegistry getGlobalRegistry() {
		return global;
	}

	public IParametersRequester getParametersRequester() {
		return requester;
	}

	@Override
	public String toString() {
		return name;
	}

}
