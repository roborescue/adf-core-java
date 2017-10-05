package adf.agent.action.ambulance;

import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;

public class ActionUnload extends Action {

	public ActionUnload() {
		super();
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionUnload []";
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		return new AKUnload(agentID, time);
	}
}
