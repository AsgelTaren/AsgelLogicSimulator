package asgel.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import asgel.app.Utils;

public class TextAtlas {

	private HashMap<String, String> data;

	public TextAtlas() {
		this.data = new HashMap<>();
	}

	public void loadDataFrom(String data) {
		this.data = new HashMap<>();
		JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			this.data.put(entry.getKey(), entry.getValue().getAsString());
		}
	}

	public void loadDataFrom(InputStream in) {
		try {
			loadDataFrom(Utils.loadFileAsString(in));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getValue(String id) {
		String res = data.get(id);
		return res == null ? id : res;
	}

}