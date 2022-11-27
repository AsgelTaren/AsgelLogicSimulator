package asgel.core.model;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import asgel.core.gfx.Direction;
import asgel.core.gfx.Point;

/**
 * @author Florent Guille
 **/

public class BundleRegistry {

	// Objects registry
	public final HashMap<String, ObjectEntry> OBJECT_REGISTRY = new HashMap<String, ObjectEntry>();

	// Tabs registry
	public final HashMap<String, ModelTab> TAB_REGISTRY = new HashMap<String, ModelTab>();

	// Text entries
	public final TextAtlas ATLAS = new TextAtlas();

	// Bundle
	private String bundle;

	public BundleRegistry(String bundle) {
		this.bundle = bundle;
	}

	public String getBundleID() {
		return bundle;
	}

	public final ModelTab registerTab(String id) {
		if (id == null || id.length() == 0)
			return null;
		if (TAB_REGISTRY.containsKey(id)) {
			// Printing conflicts to log4j
		}
		ModelTab tab = new ModelTab(id);
		tab.setName(ATLAS.getValue("tab." + tab.getID()));
		TAB_REGISTRY.put(tab.getID(), tab.setRegistry(this));
		return tab;
	}

	public final ObjectEntry registerObject(String id, String tab_id, Function<Point, ModelOBJ> provider,
			Function<JsonObject, ModelOBJ> loader) {
		if (OBJECT_REGISTRY.containsKey(id)) {

		}
		ObjectEntry entry = new ObjectEntry(id, ATLAS.getValue("object." + id), tab_id, provider, loader)
				.setRegistry(this);
		OBJECT_REGISTRY.put(id, entry);
		return entry;
	}

	public class ObjectEntry {

		private String id, name;
		private BundleRegistry registry;
		private Function<Point, ModelOBJ> provider;
		private Function<JsonObject, ModelOBJ> loader;

		private String tab;

		private BufferedImage background;

		public ObjectEntry(String id, String name, String tab, Function<Point, ModelOBJ> provider,
				Function<JsonObject, ModelOBJ> loader) {
			this.id = id;
			this.name = name;

			this.provider = p -> {
				ModelOBJ res = provider.apply(p);
				if (res != null) {
					res.setEntry(this);
					res.placePins();
				}
				return res;
			};

			this.loader = j -> {
				ModelOBJ res = loader.apply(j);
				if (res != null) {
					res.setEntry(this);
					res.placePins();
					res.setRotation(Direction.valueOf(j.get("rot").getAsString()));
					if (j.has("name"))
						res.setName(j.get("name").getAsString());
					if (j.has("moveable"))
						res.setMoveable(j.get("moveable").getAsBoolean());

					res.loadPins(j);
				}
				return res;
			};
			this.tab = tab;

		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public Function<Point, ModelOBJ> getProvider() {
			return provider;
		}

		public Function<JsonObject, ModelOBJ> getLoader() {
			return loader;
		}

		private ObjectEntry setRegistry(BundleRegistry registry) {
			this.registry = registry;
			return this;
		}

		public ObjectEntry setBackground(BufferedImage background) {
			this.background = background;
			return this;
		}

		public BufferedImage getBackground() {
			return background;
		}

		public BundleRegistry getRegistry() {
			return registry;
		}

		public String getFullID() {
			return registry.bundle + ":" + id;
		}

		public String getFullTab() {
			return registry.getBundleID() + ":" + tab;
		}

		public String getTab() {
			return tab;
		}

		public String toString() {
			return name;
		}

	}

}