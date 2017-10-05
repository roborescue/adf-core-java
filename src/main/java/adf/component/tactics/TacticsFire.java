package adf.component.tactics;

/**
 * @deprecated change class name {@link TacticsFireBrigade}
 */
@Deprecated
public abstract class TacticsFire extends TacticsFireBrigade {
	public TacticsFire(TacticsFireBrigade parent) {
		super(parent);
	}

	public TacticsFire() {
		super(null);
	}
}
