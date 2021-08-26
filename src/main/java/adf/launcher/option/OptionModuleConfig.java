package adf.launcher.option;

import rescuecore2.config.Config;

import adf.launcher.ConfigKey;

public class OptionModuleConfig extends Option {

  @Override
  public boolean hasValue() {
    return true;
  }

  @Override
  public String getKey() {
    return "-mc";
  }

  @Override
  public void setValue(Config config, String data) {
    config.setValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME, data);
  }
}
