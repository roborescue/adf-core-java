package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionPoliceOffice extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-po";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_POLICE_OFFICE_COUNT, data);
  }
}
