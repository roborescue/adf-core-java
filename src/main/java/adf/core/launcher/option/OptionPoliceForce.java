package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionPoliceForce implements Option {

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