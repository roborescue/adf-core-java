package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionModuleConfig implements Option {

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