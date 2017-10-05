package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionPoliceForce extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-pf";
	}

	@Override
	public void setValue(Config config, String data) {
		config.setValue(ConfigKey.KEY_POLICE_FORCE_COUNT, data);
	}
}
