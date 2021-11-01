package adf.agent.action.ambulance;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKUnload;
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
    return new AKUnload(agentID, time);
  }
}