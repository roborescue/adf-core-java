package adf.core.agent.office;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsFireStation;
import java.util.EnumSet;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

public class OfficeFire extends Office<Building> {

  public OfficeFire(TacticsFireStation tacticsFireStation, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(tacticsFireStation, teamName, isPrecompute,
        DATASTORAGE_FILE_NAME_FIRE, isDebugMode, moduleConfig, developData);
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.FIRE_STATION);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}