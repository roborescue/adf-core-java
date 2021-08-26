package adf.agent.platoon;

import java.util.EnumSet;

import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsPoliceForce;

public class PlatoonPolice extends Platoon<PoliceForce> {

  public PlatoonPolice(TacticsPoliceForce tactics, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig,
      DevelopData developData) {
    super(tactics, isPrecompute, DATASTORAGE_FILE_NAME_POLICE, isDebugMode, moduleConfig, developData);
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
