package adf.component.control;

import adf.component.tactics.TacticsAmbulanceCentre;

/**
 * @deprecated change class name {@link TacticsAmbulanceCentre}
 */
@Deprecated
public abstract class ControlAmbulance extends TacticsAmbulanceCentre {
	public ControlAmbulance(ControlAmbulance parent) {
		super(parent);
	}

	public ControlAmbulance() {
		this(null);
	}
}
