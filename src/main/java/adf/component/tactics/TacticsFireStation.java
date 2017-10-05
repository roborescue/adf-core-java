package adf.component.tactics;

public abstract class TacticsFireStation extends TacticsCenter {
	public TacticsFireStation(TacticsFireStation parent) {
		super(parent);
	}

	public TacticsFireStation() {
		super(null);
	}
}
