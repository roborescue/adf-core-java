package adf.launcher.connect;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.platoon.PlatoonPolice;
import adf.component.tactics.TacticsPoliceForce;
import adf.component.AbstractLoader;
import adf.launcher.ConfigKey;
import adf.launcher.ConsoleOutput;
import adf.launcher.dummy.tactics.DummyTacticsPoliceForce;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorPoliceForce extends Connector {

  @Override
  public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader) {
    int count = config.getIntValue(ConfigKey.KEY_POLICE_FORCE_COUNT, 0);

    if (count == 0) {
      return;
    }

    try {
      for (int i = 0; i != count; ++i) {
        TacticsPoliceForce tacticsPoliceForce;
        if (loader.getTacticsPoliceForce() == null) {
          ConsoleOutput.error("Cannot Load PoliceForce Tactics");
          tacticsPoliceForce = new DummyTacticsPoliceForce();
        } else {
          tacticsPoliceForce = loader.getTacticsPoliceForce();
        }

        ModuleConfig moduleConfig = new ModuleConfig(
            config.getValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME, ModuleConfig.DEFAULT_CONFIG_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_MODULE_DATA, ""));

        DevelopData developData = new DevelopData(config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false),
            config.getValue(ConfigKey.KEY_DEVELOP_DATA_FILE_NAME, DevelopData.DEFAULT_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_DEVELOP_DATA, ""));

        launcher.connect(new PlatoonPolice(tacticsPoliceForce, config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false),
            config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false), moduleConfig, developData));
        connected++;
      }
    } catch (InterruptedException | ConnectionException | ComponentConnectionException e) {
      // ConsoleOutput.finish( "[ERROR ] Cannot Load PoliceForce Tactics !!" );
    }

    ConsoleOutput.finish("Connect PoliceForce (success:" + connected + ")");
  }
}
