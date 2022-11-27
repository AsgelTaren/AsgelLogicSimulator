package asgel.core.model;

import java.util.HashMap;

import asgel.app.LaunchConfig;
import asgel.core.bundle.Bundle;
import asgel.core.bundle.Language;

/**
 * @author Florent Guille
 **/

public class GlobalRegistry {

	private Language currentLang = Language.FRENCH;

	private HashMap<String, Bundle> bundles;
	private TextAtlas appAtlas;

	public GlobalRegistry(LaunchConfig config) {
		bundles = new HashMap<>();
		appAtlas = new TextAtlas();

		if (config.getConfigJson().has("lang")) {
			currentLang = Language.valueOf(config.getConfigJson().get("lang").getAsString());
		}
	}

	public TextAtlas getAppAtlas() {
		return appAtlas;
	}

	public void loadAppTextAtlases() {
		try {
			appAtlas.loadDataFrom(GlobalRegistry.class.getClassLoader()
					.getResourceAsStream("assets/" + currentLang.getSymbol() + ".lang"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Bundle> getBundles() {
		return bundles;
	}

	public void addBundle(Bundle bundle) {
		bundles.put(bundle.getID(), bundle);
	}

	public BundleRegistry.ObjectEntry getObjectEntry(String id) {
		String[] split = id.split(":");
		return bundles.get(split[0]).getBundleRegistry().OBJECT_REGISTRY.get(split[1]);
	}

	public ModelTab getModelTab(String id) {
		String[] split = id.split(":");
		return bundles.get(split[0]).getBundleRegistry().TAB_REGISTRY.get(split[1]);
	}

	public Language getCurrentLanguage() {
		return currentLang;
	}

}