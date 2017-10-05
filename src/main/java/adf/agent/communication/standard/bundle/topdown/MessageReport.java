package adf.agent.communication.standard.bundle.topdown;

import adf.component.communication.util.BitStreamReader;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name {@link adf.agent.communication.standard.bundle.centralized.MessageReport}
 */
@Deprecated
public class MessageReport extends adf.agent.communication.standard.bundle.centralized.MessageReport {
	public MessageReport(boolean isRadio, boolean isDone, boolean isBroadcast, EntityID fromID) {
		super(isRadio, isDone, isBroadcast, fromID);
	}

	public MessageReport(boolean isRadio, int from, int ttl, BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
	}
}
