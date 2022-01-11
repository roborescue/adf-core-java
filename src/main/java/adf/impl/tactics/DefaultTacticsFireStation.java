package adf.impl.tactics;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandPicker;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.module.complex.TargetAllocator;
import adf.core.component.tactics.TacticsFireStation;
import adf.core.debug.WorldViewLauncher;
import java.util.Map;
import rescuecore2.worldmodel.EntityID;

public class DefaultTacticsFireStation extends TacticsFireStation {

  private TargetAllocator allocator;
  private CommandPicker picker;
  private Boolean isVisualDebug;

  @Override
  public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData debugData) {
    messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
        "MessageManager.CenterChannelSubscriber",
        "adf.impl.module.comm.DefaultChannelSubscriber"));
    messageManager.setMessageCoordinator(moduleManager.getMessageCoordinator(
        "MessageManager.CenterMessageCoordinator",
        "adf.impl.module.comm.DefaultMessageCoordinator"));

    switch (scenarioInfo.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        this.allocator = moduleManager.getModule(
            "TacticsFireStation.TargetAllocator",
            "adf.impl.module.complex.DefaultFireTargetAllocator");
        this.picker = moduleManager.getCommandPicker(
            "DefaultTacticsFireStation.CommandPicker",
            "adf.impl.centralized.DefaultCommandPickerFire");
        break;
    }
    registerModule(this.allocator);
    registerModule(this.picker);

    this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
        .getModuleConfig().getBooleanValue("VisualDebug", false));
  }


  @Override
  public void think(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData debugData) {
    modulesUpdateInfo(messageManager);

    if (isVisualDebug) {
      WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
          scenarioInfo);
    }
    Map<EntityID, EntityID> allocatorResult = this.allocator.calc().getResult();
    for (CommunicationMessage message : this.picker
        .setAllocatorResult(allocatorResult).calc().getResult()) {
      messageManager.addMessage(message);
    }
  }


  @Override
  public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      PrecomputeData precomputeData, DevelopData debugData) {
    modulesResume(precomputeData);

    if (isVisualDebug) {
      WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
          scenarioInfo);
    }
  }


  @Override
  public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData debugData) {
    modulesPreparate();

    if (isVisualDebug) {
      WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
          scenarioInfo);
    }
  }
}