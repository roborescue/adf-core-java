package adf.agent.platoon;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsAmbulanceTeam;
import java.util.EnumSet;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardEntityURN;

public class PlatoonAmbulance extends Platoon<AmbulanceTeam> {

  public PlatoonAmbulance( TacticsAmbulanceTeam tactics, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData ) {
    super( tactics, isPrecompute, DATASTORAGE_FILE_NAME_AMBULANCE, isDebugMode,
        moduleConfig, developData );
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of( StandardEntityURN.AMBULANCE_TEAM );
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}