package asgel.core.model;

import java.util.HashMap;

public class GlobalRegistry {

	private HashMap<String, BundleRegistry> registries;

	public GlobalRegistry() {
		registries = new HashMap<String, BundleRegistry>();
	}

	public HashMap<String, BundleRegistry> getRegistries() {
		return registries;
	}

	public BundleRegistry.ObjectEntry getObjectEntry(String id) {
		String[] split = id.split(":");
		return registries.get(split[0]).OBJECT_REGISTRY.get(split[1]);
	}

	public ModelTab getModelTab(String id) {
		String[] split = id.split(":");
		return registries.get(split[0]).TAB_REGISTRY.get(split[1]);
	}

}