package adf.core.launcher.option;

import adf.core.launcher.ConfigKey;
import java.util.Base64;
import rescuecore2.config.Config;

public class OptionDevelopData implements Option {

  private static final String DATA_DELIMITER = " ";

  @Override
  public boolean hasValue() {
    return true;
  }


  @Override
  public String getKey() {
    return "-dd";
  }


  @Override
  public void setValue(Config config, String data) {
    StringBuilder rawDevelopData = new StringBuilder(
        config.getValue(ConfigKey.KEY_DEVELOP_DATA, ""));
    rawDevelopData.append(DATA_DELIMITER);
    rawDevelopData.append(Base64.getEncoder().encodeToString(data.getBytes()));
    config.setValue(ConfigKey.KEY_DEVELOP_DATA, rawDevelopData.toString());
  }
}