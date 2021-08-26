package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionAmbulanceTeam extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-at";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_AMBULANCE_TEAM_COUNT, data);
  }
}
