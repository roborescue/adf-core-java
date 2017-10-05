package adf.launcher.option;

import rescuecore2.Constants;
import rescuecore2.config.Config;

public class OptionServer extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-s";
	}

	@Override
	public void setValue(Config config, String data) {
		String[] splitedData = data.split(":");
		if (splitedData.length == 2) {
			config.setValue(Constants.KERNEL_HOST_NAME_KEY, splitedData[0]);
			config.setValue(Constants.KERNEL_PORT_NUMBER_KEY, splitedData[1]);
		}
	}
}
