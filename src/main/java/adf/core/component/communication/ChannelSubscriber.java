package adf.core.component.communication;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;

/**
 * Created by okan on 14.01.2018.
 */
public class ChannelSubscriber {

  public void subscribe(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, MessageManager messageManager) {
    // default channel subscriber subscribes to only channel 1
    if (agentInfo.getTime() == 1) {
      int[] channels = new int[1];
      channels[0] = 1;
      messageManager.subscribeToChannels(channels);
    }
  }
}