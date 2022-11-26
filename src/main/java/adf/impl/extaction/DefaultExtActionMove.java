package adf.impl.extaction;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultExtActionMove extends ExtAction {

  private PathPlanning pathPlanning;

  private int thresholdRest;
  private int kernelTime;

  private EntityID target;

  public DefaultExtActionMove(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.target = null;
    this.thresholdRest = developData
        .getInteger("adf.impl.extaction.DefaultExtActionMove.rest", 100);

    switch (scenarioInfo.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        this.pathPlanning = moduleManager.getModule(
            "DefaultExtActionMove.PathPlanning",
            "adf.impl.module.algorithm.DijkstraPathPlanning");
        break;
    }
  }


  @Override
  public ExtAction precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.pathPlanning.precompute(precomputeData);
    try {
      this.kernelTime = this.scenarioInfo.getKernelTimesteps();
    } catch (NoSuchConfigOptionException e) {
      this.kernelTime = -1;
    }
    return this;
  }


  @Override
  public ExtAction resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    try {
      this.kernelTime = this.scenarioInfo.getKernelTimesteps();
    } catch (NoSuchConfigOptionException e) {
      this.kernelTime = -1;
    }
    return this;
  }


  @Override
  public ExtAction preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    try {
      this.kernelTime = this.scenarioInfo.getKernelTimesteps();
    } catch (NoSuchConfigOptionException e) {
      this.kernelTime = -1;
    }
    return this;
  }


  @Override
  public ExtAction updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    return this;
  }


  @Override
  public ExtAction setTarget(EntityID target) {
    this.target = null;
    StandardEntity entity = this.worldInfo.getEntity(target);
    if (entity != null) {
      if (entity.getStandardURN().equals(StandardEntityURN.BLOCKADE)) {
        entity = this.worldInfo.getEntity(((Blockade) entity).getPosition());
      } else if (entity instanceof Human) {
        entity = this.worldInfo.getPosition((Human) entity);
      }
      if (entity != null && entity instanceof Area) {
        this.target = entity.getID();
      }
    }
    return this;
  }


  @Override
  public ExtAction calc() {
    this.result = null;
    Human agent = (Human) this.agentInfo.me();

    if (this.needRest(agent)) {
      this.result = this.calcRest(agent, this.pathPlanning, this.target);
      if (this.result != null) {
        return this;
      }
    }
    if (this.target == null) {
      return this;
    }
    this.pathPlanning.setFrom(agent.getPosition());
    this.pathPlanning.setDestination(this.target);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path != null && path.size() > 0) {
      this.result = new ActionMove(path);
    }
    return this;
  }


  private boolean needRest(Human agent) {
    int hp = agent.getHP();
    int damage = agent.getDamage();
    if (hp == 0 || damage == 0) {
      return false;
    }
    int activeTime = (hp / damage) + ((hp % damage) != 0 ? 1 : 0);
    if (this.kernelTime == -1) {
      try {
        this.kernelTime = this.scenarioInfo.getKernelTimesteps();
      } catch (NoSuchConfigOptionException e) {
        this.kernelTime = -1;
      }
    }
    return damage >= this.thresholdRest
        || (activeTime + this.agentInfo.getTime()) < this.kernelTime;
  }


  private Action calcRest(Human human, PathPlanning pathPlanning,
      EntityID target) {
    EntityID position = human.getPosition();
    Collection<EntityID> refuges = this.worldInfo
        .getEntityIDsOfType(StandardEntityURN.REFUGE);
    int currentSize = refuges.size();
    if (refuges.contains(position)) {
      return new ActionRest();
    }
    List<EntityID> firstResult = null;
    while (refuges.size() > 0) {
      pathPlanning.setFrom(position);
      pathPlanning.setDestination(refuges);
      List<EntityID> path = pathPlanning.calc().getResult();
      if (path != null && path.size() > 0) {
        if (firstResult == null) {
          firstResult = new ArrayList<>(path);
          if (target == null) {
            break;
          }
        }
        EntityID refugeID = path.get(path.size() - 1);
        pathPlanning.setFrom(refugeID);
        pathPlanning.setDestination(target);
        List<EntityID> fromRefugeToTarget = pathPlanning.calc().getResult();
        if (fromRefugeToTarget != null && fromRefugeToTarget.size() > 0) {
          return new ActionMove(path);
        }
        refuges.remove(refugeID);
        // remove failed
        if (currentSize == refuges.size()) {
          break;
        }
        currentSize = refuges.size();
      } else {
        break;
      }
    }
    return firstResult != null ? new ActionMove(firstResult) : null;
  }
}