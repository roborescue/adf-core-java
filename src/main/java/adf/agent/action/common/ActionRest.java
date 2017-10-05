package adf.agent.action.common;

import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;

public class ActionRest extends Action {
	public ActionRest() {
		super();
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionRest []";
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		return new AKRest(agentID, time);
	}
}
