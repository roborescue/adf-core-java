package adf.core.agent.office;

import adf.core.agent.Agent;
import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.tactics.TacticsCenter;
import rescuecore2.standard.entities.StandardEntity;

public abstract class Office<E extends StandardEntity>extends Agent<E> {

  TacticsCenter rootTacticsCenter;

  protected Office(TacticsCenter tacticsCenter, String teamName, boolean isPrecompute, String datastorageName, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(teamName, isPrecompute, datastorageName, isDebugMode, moduleConfig,
        developData);
    this.rootTacticsCenter = tacticsCenter;
  }


  @Override
  protected void postConnect() {
    super.postConnect();

    this.agentInfo = new AgentInfo(this, model);
    this.moduleManager = new ModuleManager(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleConfig, this.developData);
    this.messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
        "MessageManager.CenterChannelSubscriber",
        "adf.core.component.communication.ChannelSubscriber"));
    this.messageManager.setMessageCoordinator(moduleManager
        .getMessageCoordinator("MessageManager.CenterMessageCoordinator",
            "adf.core.agent.communication.standard.bundle.StandardMessageCoordinator"));

    rootTacticsCenter.initialize(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleManager, this.messageManager,
        this.developData);

    switch (scenarioInfo.getMode()) {
      case NON_PRECOMPUTE:
        rootTacticsCenter.preparate(this.agentInfo, this.worldInfo,
            this.scenarioInfo, this.moduleManager, this.developData);
        break;
      case PRECOMPUTED:
        rootTacticsCenter.resume(this.agentInfo, this.worldInfo,
            this.scenarioInfo, this.moduleManager, precomputeData,
            this.developData);
        break;
      default:
    }

    this.worldInfo.registerRollbackListener();
  }


  protected void think() {
    this.rootTacticsCenter.think(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleManager, this.messageManager,
        this.developData);
  }
}
