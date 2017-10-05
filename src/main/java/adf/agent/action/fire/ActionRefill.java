package adf.agent.action.fire;

import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;

public class ActionRefill extends Action {
	public ActionRefill() {
		super();
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionRefill []";
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		return new AKRest(agentID, time);
	}
}
