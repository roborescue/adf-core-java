package adf.agent.communication.standard.bundle.topdown;

import adf.component.communication.util.BitStreamReader;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name {@link adf.agent.communication.standard.bundle.centralized.CommandAmbulance}
 */
@Deprecated
public class CommandAmbulance extends adf.agent.communication.standard.bundle.centralized.CommandAmbulance {
	public CommandAmbulance(boolean isRadio, EntityID toID, EntityID targetID, int action) {
		super(isRadio, toID, targetID, action);
	}

	public CommandAmbulance(boolean isRadio, int from, int ttl, BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
	}
}
