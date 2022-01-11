package adf.core.agent.office;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsAmbulanceCentre;
import java.util.EnumSet;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

public class OfficeAmbulance extends Office<Building> {

  public OfficeAmbulance(TacticsAmbulanceCentre tacticsAmbulanceCenter, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(tacticsAmbulanceCenter, teamName, isPrecompute,
        DATASTORAGE_FILE_NAME_AMBULANCE, isDebugMode, moduleConfig,
        developData);
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}