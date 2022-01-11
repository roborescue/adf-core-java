package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionAmbulanceTeam implements Option {

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