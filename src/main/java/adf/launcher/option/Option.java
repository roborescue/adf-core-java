package adf.launcher.option;

import rescuecore2.config.Config;

public abstract class Option {
	public abstract boolean hasValue();

	public abstract String getKey();

	public abstract void setValue(Config config, String data);
}
