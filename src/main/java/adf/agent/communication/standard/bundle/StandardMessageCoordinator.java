package adf.agent.communication.standard.bundle;

import adf.agent.communication.MessageManager;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.component.communication.CommunicationMessage;
import adf.component.communication.MessageCoordinator;

import java.util.ArrayList;
import java.util.List;

public class StandardMessageCoordinator extends MessageCoordinator {
	@Override
	public void coordinate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, MessageManager messageManager,
												 ArrayList<CommunicationMessage> sendMessageList, List<List<CommunicationMessage>> channelSendMessageList) {
		ArrayList<StandardMessage> standardRadioMessageLowList = new ArrayList<>();
		ArrayList<StandardMessage> standardRadioMessageNormalList = new ArrayList<>();
		ArrayList<StandardMessage> standardRadioMessageHighList = new ArrayList<>();

		ArrayList<StandardMessage> standardVoiceMessageLowList = new ArrayList<>();
		ArrayList<StandardMessage> standardVoiceMessageNormalList = new ArrayList<>();
		ArrayList<StandardMessage> standardVoiceMessageHighList = new ArrayList<>();

		for (CommunicationMessage msg : sendMessageList) {
			if (msg instanceof StandardMessage) {
				StandardMessage m = (StandardMessage) msg;
				switch (m.getSendingPriority()) {
					case LOW:
						if (m.isRadio()) {
							standardRadioMessageLowList.add(m);
						} else {
							standardVoiceMessageLowList.add(m);
						}
						break;
					case NORMAL:
						if (m.isRadio()) {
							standardRadioMessageNormalList.add(m);
						} else {
							standardVoiceMessageNormalList.add(m);
						}
						break;
					case HIGH:
						if (m.isRadio()) {
							standardRadioMessageHighList.add(m);
						} else {
							standardVoiceMessageHighList.add(m);
						}
						break;
				}
			}
		}

		// all radio messages are sent over the channel 1 (this is the default implementation)
		channelSendMessageList.get(1).addAll(standardRadioMessageHighList);
		channelSendMessageList.get(1).addAll(standardRadioMessageNormalList);
		channelSendMessageList.get(1).addAll(standardRadioMessageLowList);

		// set the voice channel messages
		channelSendMessageList.get(0).addAll(standardVoiceMessageHighList);
		channelSendMessageList.get(0).addAll(standardVoiceMessageNormalList);
		channelSendMessageList.get(0).addAll(standardVoiceMessageLowList);

	}
}
