package adf.component.tactics;

/**
 * @deprecated change class name {@link TacticsAmbulanceTeam}
 */
@Deprecated
public abstract class TacticsAmbulance extends TacticsAmbulanceTeam {
	public TacticsAmbulance(TacticsAmbulanceTeam parent) {
		super(parent);
	}

	public TacticsAmbulance() {
		super(null);
	}
}
