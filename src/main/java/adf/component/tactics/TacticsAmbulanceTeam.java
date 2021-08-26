package adf.component.tactics;

public abstract class TacticsAmbulanceTeam extends Tactics {

  public TacticsAmbulanceTeam(TacticsAmbulanceTeam parent) {
    super(parent);
  }

  public TacticsAmbulanceTeam() {
    super(null);
  }
}
