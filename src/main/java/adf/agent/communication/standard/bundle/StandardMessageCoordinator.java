package adf.agent.communication.standard.bundle;

import adf.agent.communication.MessageManager;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.component.communication.CommunicationMessage;
import adf.component.communication.MessageCoordinator;

import java.util.ArrayList;

public class StandardMessageCoordinator extends MessageCoordinator {
	@Override
	public void coordinate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, MessageManager messageManager, ArrayList<CommunicationMessage> sendMessageList) {
		ArrayList<StandardMessage> standardMessageLowList = new ArrayList<>();
		ArrayList<StandardMessage> standardMessageNormalList = new ArrayList<>();
		ArrayList<StandardMessage> standardMessageHighList = new ArrayList<>();
		for (CommunicationMessage msg : sendMessageList) {
			if (msg instanceof StandardMessage) {
				StandardMessage m = (StandardMessage) msg;
				switch (m.getSendingPriority()) {
					case LOW:
						standardMessageLowList.add(m);
						break;
					case NORMAL:
						standardMessageNormalList.add(m);
						break;
					case HIGH:
						standardMessageHighList.add(m);
						break;
				}
			}
		}

		sendMessageList.removeAll(standardMessageHighList);
		sendMessageList.removeAll(standardMessageNormalList);
		sendMessageList.removeAll(standardMessageLowList);

		sendMessageList.addAll(standardMessageHighList);
		sendMessageList.addAll(standardMessageNormalList);
		sendMessageList.addAll(standardMessageLowList);
	}
}
