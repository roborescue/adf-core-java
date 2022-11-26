package adf.core.component.communication;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;

import java.util.ArrayList;
import java.util.List;

abstract public class MessageCoordinator {

  abstract public void coordinate(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, MessageManager messageManager,
      ArrayList<CommunicationMessage> sendMessageList,
      List<List<CommunicationMessage>> channelSendMessageList);
}