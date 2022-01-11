package adf.core.component.module.complex;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name {@link TargetDetector}
 */
@Deprecated
public abstract class TargetSelector<E extends StandardEntity>
    extends TargetDetector<E> {

  public TargetSelector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
  }


  public abstract EntityID getTarget();


  @Override
  public TargetSelector<E> precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    return this;
  }


  @Override
  public TargetSelector<E> resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    return this;
  }


  @Override
  public TargetSelector<E> preparate() {
    super.preparate();
    return this;
  }


  @Override
  public TargetSelector<E> updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    return this;
  }


  @Override
  public abstract TargetSelector<E> calc();
}