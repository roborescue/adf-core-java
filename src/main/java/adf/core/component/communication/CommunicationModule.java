package adf.core.component.communication;

import adf.core.agent.Agent;
import adf.core.agent.communication.MessageManager;

abstract public class CommunicationModule {

  abstract public void receive(Agent agent, MessageManager messageManager);

  abstract public void send(Agent agent, MessageManager messageManager);
}