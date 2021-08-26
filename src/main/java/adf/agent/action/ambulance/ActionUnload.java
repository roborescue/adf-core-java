package adf.agent.action.ambulance;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.messages.control.AKCommand;
import rescuecore2.standard.commands.AKUnload;
import rescuecore2.worldmodel.EntityID;

import adf.agent.action.Action;

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
    return new AKCommand(new AKUnload(agentID, time));
  }
}
