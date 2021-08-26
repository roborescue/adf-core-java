package adf.agent.action.fire;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.messages.control.AKCommand;
import rescuecore2.standard.commands.AKRest;
import rescuecore2.worldmodel.EntityID;

import adf.agent.action.Action;

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
    return new AKCommand(new AKRest(agentID, time));
  }
}
