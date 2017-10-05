package adf.launcher.option;

import rescuecore2.Constants;
import rescuecore2.config.Config;

public class OptionHost extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-h";
	}

	@Override
	public void setValue(Config config, String data) {
		config.setValue(Constants.KERNEL_HOST_NAME_KEY, data);
	}
}
