package adf.core.agent.platoon;

import adf.core.agent.Agent;
import adf.core.agent.action.Action;
import adf.core.agent.config.ModuleConfig;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.tactics.Tactics;
import adf.core.launcher.ConsoleOutput;
import rescuecore2.standard.entities.StandardEntity;

public abstract class Platoon<E extends StandardEntity>extends Agent<E> {

  private Tactics rootTactics;

  Platoon(Tactics tactics, String teamName, boolean isPrecompute, String dataStorageName, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
    super(teamName, isPrecompute, dataStorageName, isDebugMode, moduleConfig,
        developData);
    this.rootTactics = tactics;
  }


  @Override
  protected void postConnect() {
    super.postConnect();

    this.agentInfo = new AgentInfo(this, this.model);
    this.moduleManager = new ModuleManager(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleConfig, this.developData);
    this.messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
        "MessageManager.PlatoonChannelSubscriber",
        "adf.component.communication.ChannelSubscriber"));
    this.messageManager.setMessageCoordinator(moduleManager
        .getMessageCoordinator("MessageManager.PlatoonMessageCoordinator",
            "adf.core.agent.communication.standard.bundle.StandardMessageCoordinator"));

    this.rootTactics.initialize(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleManager, this.messageManager,
        this.developData);

    switch (this.scenarioInfo.getMode()) {
      case NON_PRECOMPUTE:
        this.rootTactics.preparate(this.agentInfo, this.worldInfo,
            this.scenarioInfo, this.moduleManager, this.developData);
        this.worldInfo.registerRollbackListener();
        break;
      case PRECOMPUTATION_PHASE:
        this.rootTactics.precompute(this.agentInfo, this.worldInfo,
            this.scenarioInfo, this.moduleManager, this.precomputeData,
            this.developData);
        this.precomputeData.setReady(true, this.worldInfo);
        if (!this.precomputeData.write()) {
          ConsoleOutput.out(ConsoleOutput.State.ERROR,
              "[ERROR ] Failed to write PrecomputeData.");
        }
        break;
      case PRECOMPUTED:
        this.rootTactics.resume(this.agentInfo, this.worldInfo,
            this.scenarioInfo, this.moduleManager, this.precomputeData,
            this.developData);
        this.worldInfo.registerRollbackListener();
        break;
      default:
    }
  }


  protected void think() {
    Action action = this.rootTactics.think(this.agentInfo, this.worldInfo,
        this.scenarioInfo, this.moduleManager, this.messageManager,
        this.developData);
    if (action != null) {
      this.agentInfo.setExecutedAction(this.agentInfo.getTime(), action);
      send(action.getCommand(this.getID(), this.agentInfo.getTime()));
    }
  }
}