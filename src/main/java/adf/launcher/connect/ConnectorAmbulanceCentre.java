package adf.launcher.connect;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.office.OfficeAmbulance;
import adf.component.AbstractLoader;
import adf.component.tactics.TacticsAmbulanceCentre;
import adf.launcher.ConfigKey;
import adf.launcher.ConsoleOutput;
import adf.launcher.dummy.tactics.center.DummyTacticsAmbulanceCentre;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorAmbulanceCentre extends Connector {
	@Override
	public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader) {
		int count = config.getIntValue(ConfigKey.KEY_AMBULANCE_CENTRE_COUNT, 0);

		if (count == 0) {
			return;
		}

		try {
			for (int i = 0; i != count; ++i) {
				TacticsAmbulanceCentre tacticsAmbulanceCenter;
				if (loader.getTacticsAmbulanceCentre() == null) {
					ConsoleOutput.error("Cannot Load AmbulanceCentre Tactics");
					tacticsAmbulanceCenter = new DummyTacticsAmbulanceCentre();
				} else {
					tacticsAmbulanceCenter = loader.getTacticsAmbulanceCentre();
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

				launcher.connect(new OfficeAmbulance(
					tacticsAmbulanceCenter,
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
			//System.out.println("[ERROR ] Cannot Load AmbulanceCentre Control !!");
		}

		ConsoleOutput.finish("Connect AmbulanceCentre (success:" + connected + ")");
	}
}
