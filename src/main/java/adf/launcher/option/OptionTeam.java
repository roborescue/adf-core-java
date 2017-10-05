package adf.launcher.option;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionTeam extends Option {
	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getKey() {
		return "-t";
	}

	@Override
	public void setValue(Config config, String data) {
		String[] splitedData = data.split(",");
		if (splitedData.length == 6) {
			config.setValue(ConfigKey.KEY_FIRE_BRIGADE_COUNT, splitedData[0]);
			config.setValue(ConfigKey.KEY_FIRE_STATION_COUNT, splitedData[1]);

			config.setValue(ConfigKey.KEY_POLICE_FORCE_COUNT, splitedData[2]);
			config.setValue(ConfigKey.KEY_POLICE_OFFICE_COUNT, splitedData[3]);

			config.setValue(ConfigKey.KEY_AMBULANCE_TEAM_COUNT, splitedData[4]);
			config.setValue(ConfigKey.KEY_AMBULANCE_CENTRE_COUNT, splitedData[5]);
		}
	}
}
