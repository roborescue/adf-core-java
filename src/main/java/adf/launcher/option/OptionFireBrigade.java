package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionFireBrigade extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-fb";
	}

	@Override
	public void setValue(Config config, String data) {
		config.setValue(ConfigKey.KEY_FIRE_BRIGADE_COUNT, data);
	}
}
