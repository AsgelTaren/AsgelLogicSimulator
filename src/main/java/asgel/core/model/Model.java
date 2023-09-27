package asgel.core.model;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import asgel.core.gfx.Point;
import asgel.core.model.BundleRegistry.ObjectEntry;

/**
 * @author Florent Guille
 **/

public class Model {

	private ArrayList<ModelOBJ> objects;
	private ArrayList<Link> links;

	private HashMap<String, String> catIcons;

	private File file;

	public Model(File file) {
		objects = new ArrayList<ModelOBJ>();
		links = new ArrayList<Link>();
		catIcons = new HashMap<>();
		this.file = file;
	}

	public Model(JsonObject obj, GlobalRegistry regis, File file) throws MissingBundleException {
		objects = new ArrayList<ModelOBJ>();
		links = new ArrayList<Link>();
		catIcons = new HashMap<>();
		this.file = file;
		load(obj, regis);

	}

	public Model(File f, GlobalRegistry regis) throws Exception {
		objects = new ArrayList<ModelOBJ>();
		links = new ArrayList<Link>();
		catIcons = new HashMap<>();
		this.file = f;
		load(JsonParser.parseReader(new FileReader(f)).getAsJsonObject(), regis);
	}

	public void refresh(List<? extends ModelOBJ> start) {
		ArrayList<ModelOBJ> toCheck = new ArrayList<ModelOBJ>(start);

		while (!toCheck.isEmpty()) {
			ArrayList<ModelOBJ> next = new ArrayList<ModelOBJ>();
			for (ModelOBJ obj : toCheck) {
				obj.update();
				for (Pin p : obj.getPins()) {
					if (!(p.isInput() || p.getLink() == null || p.getLink().isCoherent())) {
						p.getLink().transmitData();
						if (!next.contains(p.getLink().getEnd().getModelOBJ()) && p.getLink().getEnd().isSensible()) {
							next.add(p.getLink().getEnd().getModelOBJ());
						}
					}
				}
				toCheck = next;
			}
		}
	}

	public ArrayList<ModelOBJ> getObjects() {
		return objects;
	}

	public ArrayList<Link> getLinks() {
		return links;
	}

	public JsonObject convertToJson() {
		JsonObject res = new JsonObject();

		Set<String> usedBundles = new HashSet<String>();

		JsonArray objArr = new JsonArray();
		for (ModelOBJ obj : objects) {
			JsonObject json = obj.toJson();
			json.add("pinsData", obj.pinsToJson());
			usedBundles.add(obj.getEntry().getRegistry().getBundleID());
			objArr.add(json);
		}

		JsonArray linkArr = new JsonArray();
		for (Link link : links) {
			JsonObject json = new JsonObject();
			json.addProperty("startPin", link.getStart().getModelOBJ().indexOfPin(link.getStart()));
			json.addProperty("endPin", link.getEnd().getModelOBJ().indexOfPin(link.getEnd()));
			json.addProperty("startOBJ", objects.indexOf(link.getStart().getModelOBJ()));
			json.addProperty("endOBJ", objects.indexOf(link.getEnd().getModelOBJ()));
			Color color = link.getColor();
			if (color != null) {
				JsonArray arr = new JsonArray();
				arr.add(color.getRed());
				arr.add(color.getGreen());
				arr.add(color.getBlue());
				arr.add(color.getAlpha());
				json.add("color", arr);
			}
			if (link.getPath() != null) {
				JsonArray path = new JsonArray();
				for (Point p : link.getPath()) {
					path.add(p.toJson());
				}
				json.add("path", path);
			}
			linkArr.add(json);
		}

		JsonObject catIconsOBJ = new JsonObject();
		for (Entry<String, String> entry : catIcons.entrySet()) {
			catIconsOBJ.addProperty(entry.getKey(), entry.getValue());
		}

		JsonArray bundleArr = new JsonArray();
		for (String s : usedBundles) {
			bundleArr.add(s);
		}

		res.add("bundles", bundleArr);
		res.add("objects", objArr);
		res.add("links", linkArr);
		res.add("catIcons", catIconsOBJ);
		return res;
	}

	private void load(JsonObject json, GlobalRegistry regis) throws MissingBundleException {
		if (json.has("bundles"))
			for (JsonElement e : json.get("bundles").getAsJsonArray()) {
				if (!regis.getBundles().containsKey(e.getAsString())) {
					throw new MissingBundleException("Missing " + e.getAsString());
				}
			}

		for (JsonElement e : json.get("objects").getAsJsonArray()) {
			JsonObject obj = e.getAsJsonObject();
			ObjectEntry entry = regis.getObjectEntry(obj.get("entry").getAsString());
			objects.add(entry.getLoader().apply(new ObjectData(obj, this)));
		}
		for (JsonElement e : json.get("links").getAsJsonArray()) {
			JsonObject obj = e.getAsJsonObject();
			Link l = Link.create(objects.get(obj.get("startOBJ").getAsInt()).getPins()[obj.get("startPin").getAsInt()],
					objects.get(obj.get("endOBJ").getAsInt()).getPins()[obj.get("endPin").getAsInt()]);
			if(l == null)continue;
			if (obj.has("path")) {
				l.setPathFromJson(obj.get("path").getAsJsonArray());
			}
			if (obj.has("color")) {
				JsonArray arr = obj.get("color").getAsJsonArray();
				Color c = new Color(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt(),
						arr.get(3).getAsInt());
				l.setColor(c);
			}
			l.getStart().setLink(l);
			l.getEnd().setLink(l);
			links.add(l);
		}
		if (json.has("catIcons"))
			for (Entry<String, JsonElement> entry : json.get("catIcons").getAsJsonObject().entrySet()) {
				catIcons.put(entry.getKey(), entry.getValue().getAsString());
			}
	}

	public HashMap<String, String> getCatIcons() {
		return catIcons;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}