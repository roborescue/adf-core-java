package adf.core.agent.platoon;

import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.component.tactics.TacticsFireBrigade;
import java.util.EnumSet;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntityURN;

public class PlatoonFire extends Platoon<FireBrigade> {

  public PlatoonFire(TacticsFireBrigade tactics, String teamName, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(tactics, teamName, isPrecompute, DATASTORAGE_FILE_NAME_FIRE,
        isDebugMode, moduleConfig, developData);
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
  }


  @Override
  protected void postConnect() {
    super.postConnect();
  }
}