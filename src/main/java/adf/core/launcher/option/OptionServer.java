package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import rescuecore2.config.Config;

public class OptionServer implements Option {

  @Override
  public boolean hasValue() {
    return true;
  }


  @Override
  public String getKey() {
    return "-s";
  }


  @Override
  public void setValue(Config config, String data) {
    String[] splitedData = data.split(":");
    if (splitedData.length == 2) {
      config.setValue(ConfigKey.KEY_KERNEL_HOST, splitedData[0]);
      config.setValue(ConfigKey.KEY_KERNEL_PORT, splitedData[1]);
    }
  }
}