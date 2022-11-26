package adf.core.launcher;

public final class ConfigKey {

  // General
  public static final String KEY_LOADER_CLASS = "adf.launcher.loader";

  public static final String KEY_KERNEL_HOST = "kernel.host";
  public static final String KEY_KERNEL_PORT = "kernel.port";

  public static final String KEY_TEAM_NAME = "team.name";

  public static final String KEY_DEBUG_FLAG = "adf.debug.flag";

  public static final String KEY_DEVELOP_FLAG = "adf.develop.flag";
  public static final String KEY_DEVELOP_DATA_FILE_NAME = "adf.develop.filename";
  public static final String KEY_DEVELOP_DATA = "adf.develop.data";

  public static final String KEY_MODULE_CONFIG_FILE_NAME = "adf.agent.moduleconfig.filename";
  public static final String KEY_MODULE_DATA = "adf.agent.moduleconfig.data";

  public static final String KEY_PRECOMPUTE = "adf.launcher.precompute";

  // Platoon
  public static final String KEY_AMBULANCE_TEAM_COUNT = "adf.team.platoon.ambulance.count";
  public static final String KEY_FIRE_BRIGADE_COUNT = "adf.team.platoon.fire.count";
  public static final String KEY_POLICE_FORCE_COUNT = "adf.team.platoon.police.count";

  // Office
  public static final String KEY_AMBULANCE_CENTRE_COUNT = "adf.team.office.ambulance.count";
  public static final String KEY_FIRE_STATION_COUNT = "adf.team.office.fire.count";
  public static final String KEY_POLICE_OFFICE_COUNT = "adf.team.office.police.count";
}