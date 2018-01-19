package adf.component.communication;

import adf.agent.communication.MessageManager;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;

/**
 * Created by okan on 14.01.2018.
 */
public class ChannelSubscriber {
	public void subscribe(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
																 MessageManager messageManager) {
		// default channel subscriber subscribes to only channel 1
		if (agentInfo.getTime() == 1) {
			int[] channels = new int[1];
			channels[0] = 1;
			messageManager.subscribeToChannels(channels);
		}
	}
}
