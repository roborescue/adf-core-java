package adf.component.tactics;

/**
 * @deprecated change class name {@link TacticsPoliceForce}
 */
@Deprecated
public abstract class TacticsPolice extends TacticsPoliceForce {
	public TacticsPolice(TacticsPoliceForce parent) {
		super(parent);
	}

	public TacticsPolice() {
		super(null);
	}
}
