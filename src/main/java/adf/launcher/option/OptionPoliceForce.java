package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionPoliceForce extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-pf";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_POLICE_FORCE_COUNT, data);
  }
}
