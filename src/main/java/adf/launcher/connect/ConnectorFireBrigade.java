package adf.launcher.connect;

import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.platoon.PlatoonFire;
import adf.component.AbstractLoader;
import adf.component.tactics.TacticsFireBrigade;
import adf.launcher.ConfigKey;
import adf.launcher.ConsoleOutput;
import adf.launcher.dummy.tactics.DummyTacticsFireBrigade;

public class ConnectorFireBrigade extends Connector {

  @Override
  public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader) {
    int count = config.getIntValue(ConfigKey.KEY_FIRE_BRIGADE_COUNT, 0);

    if (count == 0) {
      return;
    }

    try {
      for (int i = 0; i != count; ++i) {
        TacticsFireBrigade tacticsFireBrigade;
        if (loader.getTacticsFireBrigade() == null) {
          ConsoleOutput.error("Cannot Load FireBrigade Tactics");
          tacticsFireBrigade = new DummyTacticsFireBrigade();
        } else {
          tacticsFireBrigade = loader.getTacticsFireBrigade();
        }

        ModuleConfig moduleConfig = new ModuleConfig(
            config.getValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME, ModuleConfig.DEFAULT_CONFIG_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_MODULE_DATA, ""));

        DevelopData developData = new DevelopData(config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false),
            config.getValue(ConfigKey.KEY_DEVELOP_DATA_FILE_NAME, DevelopData.DEFAULT_FILE_NAME),
            config.getArrayValue(ConfigKey.KEY_DEVELOP_DATA, ""));

        launcher.connect(new PlatoonFire(tacticsFireBrigade, config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false),
            config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false), moduleConfig, developData));
        connected++;
      }
    } catch (ComponentConnectionException | InterruptedException | ConnectionException e) {
      // ConsoleOutput.finish( "[ERROR ] Cannot Load FireBrigade Tactics !!" );
    }

    ConsoleOutput.finish("Connect FireBrigade (success:" + connected + ")");
  }
}
