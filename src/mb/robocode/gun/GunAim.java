package mb.robocode.gun;

import mb.robocode.vector.Vector;

public class GunAim {
  public final Vector aimVector;
  public final double power;
  
  public GunAim(final Vector aimVector, final double power) {
    this.aimVector = aimVector;
    this.power = power;
  }

}
