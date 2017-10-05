package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionAmbulanceCentre extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-ac";
	}

	@Override
	public void setValue(Config config, String data) {
		config.setValue(ConfigKey.KEY_AMBULANCE_CENTRE_COUNT, data);
	}
}
