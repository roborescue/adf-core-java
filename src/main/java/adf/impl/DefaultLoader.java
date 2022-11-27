package adf.impl;

import adf.core.component.AbstractLoader;
import adf.core.component.tactics.*;
import adf.impl.tactics.*;

/**
 *
 */
public class DefaultLoader extends AbstractLoader {

  @Override
  public TacticsAmbulanceTeam getTacticsAmbulanceTeam() {
    return new DefaultTacticsAmbulanceTeam();
  }


  @Override
  public TacticsFireBrigade getTacticsFireBrigade() {
    return new DefaultTacticsFireBrigade();
  }


  @Override
  public TacticsPoliceForce getTacticsPoliceForce() {
    return new DefaultTacticsPoliceForce();
  }


  @Override
  public TacticsAmbulanceCentre getTacticsAmbulanceCentre() {
    return new DefaultTacticsAmbulanceCentre();
  }


  @Override
  public TacticsFireStation getTacticsFireStation() {
    return new DefaultTacticsFireStation();
  }


  @Override
  public TacticsPoliceOffice getTacticsPoliceOffice() {
    return new DefaultTacticsPoliceOffice();
  }
}