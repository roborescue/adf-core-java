package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionDebug extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-d";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_DEBUG_FLAG, data);
  }
}
