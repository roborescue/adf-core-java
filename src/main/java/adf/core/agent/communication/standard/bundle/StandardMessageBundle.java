package adf.core.agent.communication.standard.bundle;

import adf.core.agent.communication.standard.bundle.centralized.CommandAmbulance;
import adf.core.agent.communication.standard.bundle.centralized.CommandFire;
import adf.core.agent.communication.standard.bundle.centralized.CommandPolice;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
import adf.core.agent.communication.standard.bundle.centralized.MessageReport;
import adf.core.agent.communication.standard.bundle.information.MessageAmbulanceTeam;
import adf.core.agent.communication.standard.bundle.information.MessageBuilding;
import adf.core.agent.communication.standard.bundle.information.MessageCivilian;
import adf.core.agent.communication.standard.bundle.information.MessageFireBrigade;
import adf.core.agent.communication.standard.bundle.information.MessagePoliceForce;
import adf.core.agent.communication.standard.bundle.information.MessageRoad;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.communication.MessageBundle;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class StandardMessageBundle extends MessageBundle {

  @Override
  @Nonnull
  public List<Class<? extends CommunicationMessage>> getMessageClassList() {
    List<Class<
        ? extends CommunicationMessage>> messageClassList = new ArrayList<>();

    // information
    messageClassList.add(MessageAmbulanceTeam.class);
    messageClassList.add(MessageBuilding.class);
    messageClassList.add(MessageCivilian.class);
    messageClassList.add(MessageFireBrigade.class);
    messageClassList.add(MessagePoliceForce.class);
    messageClassList.add(MessageRoad.class);
    // centralized
    messageClassList.add(CommandAmbulance.class);
    messageClassList.add(CommandFire.class);
    messageClassList.add(CommandPolice.class);
    messageClassList.add(CommandScout.class);
    messageClassList.add(MessageReport.class);

    return messageClassList;
  }
}