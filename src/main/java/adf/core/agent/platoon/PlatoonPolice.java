package adf.core.agent.platoon;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsPoliceForce;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class PlatoonPolice extends Platoon<PoliceForce> {

    public PlatoonPolice(TacticsPoliceForce tactics, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
        super(tactics, teamName, isPrecompute, DATASTORAGE_FILE_NAME_POLICE,
                isDebugMode, moduleConfig, developData);
    }


    @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.POLICE_FORCE);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}