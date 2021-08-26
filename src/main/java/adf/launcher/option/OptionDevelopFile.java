package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionDevelopFile extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-df";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_DEVELOP_DATA_FILE_NAME, data);
  }
}
