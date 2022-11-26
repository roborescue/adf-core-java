package adf.core.component.module.complex;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;

/**
 * @deprecated change class name {@link BuildingDetector}
 */
@Deprecated
public abstract class BuildingSelector extends BuildingDetector {

  public BuildingSelector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
  }


  @Override
  public BuildingSelector precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    return this;
  }


  @Override
  public BuildingSelector resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    return this;
  }


  @Override
  public BuildingSelector preparate() {
    super.preparate();
    return this;
  }


  @Override
  public BuildingSelector updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    return this;
  }


  @Override
  public abstract BuildingSelector calc();
}