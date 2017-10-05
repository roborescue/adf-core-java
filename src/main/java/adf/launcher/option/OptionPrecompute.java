package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionPrecompute extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-pre";
	}

	@Override
	public void setValue(Config config, String data) {
		config.setValue(ConfigKey.KEY_PRECOMPUTE, data);
	}
}
