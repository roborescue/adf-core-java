package adf.component.control;

import adf.component.tactics.TacticsPoliceOffice;

/**
 * @deprecated change class name {@link TacticsPoliceOffice}
 */
@Deprecated
public abstract class ControlPolice extends TacticsPoliceOffice {
	public ControlPolice(ControlPolice parent) {
		super(parent);
	}

	public ControlPolice() {
		super(null);
	}
}
