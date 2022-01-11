package adf.core.launcher.dummy.tactics.center;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.tactics.TacticsFireStation;

public class DummyTacticsFireStation extends TacticsFireStation {

  @Override
  public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData) {

  }


  @Override
  public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      PrecomputeData precomputeInfo, DevelopData developData) {

  }


  @Override
  public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData developData) {

  }


  @Override
  public void think(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData) {

  }
}