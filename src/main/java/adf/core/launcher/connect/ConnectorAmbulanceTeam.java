package adf.core.launcher.connect;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.platoon.PlatoonAmbulance;
import adf.core.component.AbstractLoader;
import adf.core.component.tactics.TacticsAmbulanceTeam;
import adf.core.launcher.ConfigKey;
import adf.core.launcher.ConsoleOutput;
import adf.core.launcher.dummy.tactics.DummyTacticsAmbulanceTeam;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorAmbulanceTeam extends Connector {

  @Override
  public void connect(ComponentLauncher launcher, Config config,
      AbstractLoader loader) {
    int count = config.getIntValue(ConfigKey.KEY_AMBULANCE_TEAM_COUNT, 0);

    if (count == 0) {
      return;
    }

    // connect
    try {
      for (int i = 0; i != count; ++i) {
        TacticsAmbulanceTeam tacticsAmbulanceTeam;
        if (loader.getTacticsAmbulanceTeam() == null) {
          ConsoleOutput.error("Cannot Load AmbulanceTeam Tactics");
          tacticsAmbulanceTeam = new DummyTacticsAmbulanceTeam();
        } else {
          tacticsAmbulanceTeam = loader.getTacticsAmbulanceTeam();
        }

        ModuleConfig moduleConfig = new ModuleConfig(
            config.getValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME,
                ModuleConfig.DEFAULT_CONFIG_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_MODULE_DATA, ""));

        DevelopData developData = new DevelopData(
            config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false),
            config.getValue(ConfigKey.KEY_DEVELOP_DATA_FILE_NAME,
                DevelopData.DEFAULT_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_DEVELOP_DATA, ""));

        launcher.connect(new PlatoonAmbulance(tacticsAmbulanceTeam,
            config.getValue(ConfigKey.KEY_TEAM_NAME, "not_set"),
            config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false),
            config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false),
            moduleConfig, developData));
        connected++;
      }
    } catch (ComponentConnectionException | InterruptedException
        | ConnectionException e) {
    }

    ConsoleOutput.finish("Connect AmbulanceTeam (success:" + connected + ")");
  }
}