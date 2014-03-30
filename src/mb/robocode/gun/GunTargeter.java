package mb.robocode.gun;

import mb.robocode.movement.MovementEstimator;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;

public interface GunTargeter {
  public GunAim getAimVector(Target target, Vector curPos, long time, MovementEstimator movementEstimator);
}