package asgel.app.bundle;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.ImageIcon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import asgel.app.Utils;
import asgel.core.bundle.RessourceManager;
import asgel.core.model.BundleRegistry;

public class Bundle {

	private File file;
	private ImageIcon icon;
	private String name, id, desc, main;

	public Bundle(String id) {
		this.id = id;
		this.file = new File(System.getenv("APPDATA") + "/AsgelLogicSim/bundles/" + id + ".jar");
	}

	public Bundle(File f) {
		this.file = f;
		this.id = f.getName().substring(0, f.getName().length() - 4);
	}

	public void loadDetails() throws Exception {
		JsonObject obj = JsonParser.parseString(new String(Utils.openStreamInJar(file, "bundle.json").readAllBytes()))
				.getAsJsonObject();
		this.name = obj.get("name").getAsString();
		this.desc = obj.get("desc").getAsString();
		this.main = obj.get("main").getAsString();

		try {
			icon = asgel.core.bundle.Utils.loadIcon(Utils.openStreamInJar(file, "logo.png"), 16);
		} catch (Exception e) {
			System.out.println("Failed to load logo.png for" + id);
		}
	}

	public void load(BundleRegistry registry) throws Exception {
		URLClassLoader child = new URLClassLoader(new URL[] { file.toURI().toURL() }, this.getClass().getClassLoader());
		Class<?> bundle = Class.forName(main + ".Bundle", true, child);
		Method method = bundle.getDeclaredMethod("loadBundle", BundleRegistry.class, RessourceManager.class);
		Object instance = bundle.getConstructor().newInstance();
		RessourceManager res = new RessourceManager(bundle);
		method.invoke(instance, registry, res);
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

	@Override
	public String toString() {
		return name;
	}

}