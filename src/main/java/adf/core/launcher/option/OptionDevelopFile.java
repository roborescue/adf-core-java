package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionDevelopFile implements Option {

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