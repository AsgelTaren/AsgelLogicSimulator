package asgel.core.model;

import com.google.gson.JsonObject;

public class ObjectData {

	public JsonObject json;
	public Model model;

	public ObjectData(JsonObject json, Model model) {
		this.json = json;
		this.model = model;
	}

}
