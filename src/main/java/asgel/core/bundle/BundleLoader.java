package asgel.core.bundle;

import asgel.core.model.BundleRegistry;
import asgel.core.model.IParametersRequester;

public interface BundleLoader {

	public void loadBundle(BundleRegistry registry, RessourceManager res, IParametersRequester requester);

}