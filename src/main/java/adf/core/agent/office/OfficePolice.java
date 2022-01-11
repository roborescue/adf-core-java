package adf.core.agent.office;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsPoliceOffice;
import java.util.EnumSet;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

public class OfficePolice extends Office<Building> {

  public OfficePolice(TacticsPoliceOffice tacticsPoliceOffice, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(tacticsPoliceOffice, teamName, isPrecompute,
        DATASTORAGE_FILE_NAME_POLICE, isDebugMode, moduleConfig, developData);
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}