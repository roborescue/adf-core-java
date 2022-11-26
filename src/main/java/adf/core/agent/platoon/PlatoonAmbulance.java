package adf.core.agent.platoon;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsAmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class PlatoonAmbulance extends Platoon<AmbulanceTeam> {

    public PlatoonAmbulance(TacticsAmbulanceTeam tactics, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
        super(tactics, teamName, isPrecompute, DATASTORAGE_FILE_NAME_AMBULANCE,
                isDebugMode, moduleConfig, developData);
    }


    @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}