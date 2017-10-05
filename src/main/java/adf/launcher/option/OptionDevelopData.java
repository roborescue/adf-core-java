package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

import java.util.Base64;

public class OptionDevelopData extends Option {
	private static final String DATA_DELIMITER = " ";

	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-dd";
	}

	@Override
	public void setValue(Config config, String data) {
		StringBuilder rawDevelopData = new StringBuilder(config.getValue(ConfigKey.KEY_DEVELOP_DATA, ""));
		rawDevelopData.append(DATA_DELIMITER);
		rawDevelopData.append(Base64.getEncoder().encodeToString(data.getBytes()));
		config.setValue(ConfigKey.KEY_DEVELOP_DATA, rawDevelopData.toString());
	}
}
