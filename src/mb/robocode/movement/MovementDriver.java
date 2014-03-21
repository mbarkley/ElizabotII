package mb.robocode.movement;

import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitRobotEvent;

public interface MovementDriver {
  public Vector movement(Vector curPos, Target target, long time);

  public void onHitRobot(HitRobotEvent event);
}