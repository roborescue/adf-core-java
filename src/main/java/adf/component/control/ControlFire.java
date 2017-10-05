package adf.component.control;

import adf.component.tactics.TacticsFireStation;

/**
 * @deprecated change class name {@link TacticsFireStation}
 */
@Deprecated
public abstract class ControlFire extends TacticsFireStation {
	public ControlFire(ControlFire parent) {
		super(parent);
	}

	public ControlFire() {
		this(null);
	}
}
