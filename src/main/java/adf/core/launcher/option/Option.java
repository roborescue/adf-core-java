package adf.core.launcher.option;

import rescuecore2.config.Config;

public interface Option {

  public boolean hasValue();

  public String getKey();

  public void setValue(Config config, String data);
}