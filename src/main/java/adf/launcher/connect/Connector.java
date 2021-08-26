package adf.launcher.connect;

import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;

import adf.component.AbstractLoader;

public abstract class Connector {
  int connected = 0;

  public abstract void connect(ComponentLauncher launcher, Config config, AbstractLoader loader);

  public int getCountConnected() {
    return connected;
  }
}
