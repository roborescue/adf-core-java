package adf.component.communication;


import adf.agent.Agent;
import adf.agent.communication.MessageManager;

abstract public class CommunicationModule {
	abstract public void receive(Agent agent, MessageManager messageManager);

	abstract public void send(Agent agent, MessageManager messageManager);
}
