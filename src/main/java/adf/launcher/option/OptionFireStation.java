package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionFireStation extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-fs";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_FIRE_STATION_COUNT, data);
  }
}
