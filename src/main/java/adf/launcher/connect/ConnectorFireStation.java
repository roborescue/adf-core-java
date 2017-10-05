package adf.launcher.connect;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.office.OfficeFire;
import adf.component.AbstractLoader;
import adf.component.tactics.TacticsFireStation;
import adf.launcher.ConfigKey;
import adf.launcher.ConsoleOutput;
import adf.launcher.dummy.tactics.center.DummyTacticsFireStation;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorFireStation extends Connector {
	@Override
	public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader) {
		int count = config.getIntValue(ConfigKey.KEY_FIRE_STATION_COUNT, 0);

		if (count == 0) {
			return;
		}

		try {
			for (int i = 0; i != count; ++i) {
				TacticsFireStation tacticsFireStation;
				if (loader.getTacticsFireStation() == null) {
					ConsoleOutput.error("Cannot Load FireStation Tactics");
					tacticsFireStation = new DummyTacticsFireStation();
				} else {
					tacticsFireStation = loader.getTacticsFireStation();
				}

				ModuleConfig moduleConfig = new ModuleConfig(
					config.getValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME, ModuleConfig.DEFAULT_CONFIG_FILE_NAME),
					config.getArrayValue(ConfigKey.KEY_MODULE_DATA, "")
				);

				DevelopData developData = new DevelopData(
					config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false),
					config.getValue(ConfigKey.KEY_DEVELOP_DATA_FILE_NAME, DevelopData.DEFAULT_FILE_NAME),
					config.getArrayValue(ConfigKey.KEY_DEVELOP_DATA, "")
				);

				launcher.connect(new OfficeFire(
					tacticsFireStation,
					config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false),
					config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false),
					moduleConfig,
					developData
				));
				//System.out.println(name);
				connected++;
			}
		} catch (ComponentConnectionException | InterruptedException | ConnectionException e) {
			//e.printStackTrace();
			//System.out.println("[ERROR ] Cannot Load FireStation Control !!");
		}

		ConsoleOutput.finish("Connect FireStation (success:" + connected + ")");
	}
}
