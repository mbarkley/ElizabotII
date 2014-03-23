package mb.robocode.movement.impl;

import mb.robocode.movement.MovementDriver;
import mb.robocode.movement.MovementEstimator;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;

public class FewDriver implements MovementDriver {

  private final double dist;
  private final double buffer;
  private double sign = 1.0;
  private final Vector battleFieldBound;

  private final MovementEstimator movementEstimator;

  public FewDriver(final double dist, final Vector battleFieldBound,
      final Vector robotBound, final MovementEstimator movementEstimator) {
    this.dist = dist;
    this.movementEstimator = movementEstimator;
    this.battleFieldBound = battleFieldBound;
    this.buffer = 1.5 * Math.sqrt(robotBound.y * robotBound.y + robotBound.x
        * robotBound.x);
  }

  private boolean isAwayFromEdge(final Vector pos) {
    final Vector restrictedTopRight = new Vector(battleFieldBound.x
        - buffer, battleFieldBound.y - buffer);
    final Vector restrictedBottomLeft = new Vector(buffer, buffer);

    return pos.isBoundBy(restrictedTopRight)
        && restrictedBottomLeft.isBoundBy(pos);
  }

  @Override
  public Vector movement(final Vector curPos, final Target curTarget,
      final long time) {
    if (curTarget != null) {
      final Vector pos = movementEstimator.estimatePosition(curTarget,
          (int) (time - curTarget.time));
      final Vector rawVector = pos.minus(curPos);
      Vector vector = rawVector
          .normalize()
          .scale(2 * Rules.MAX_VELOCITY);

      final double angle = Math.PI / 2.0
          * (Math.pow(Math.max(0.0, dist - rawVector.abs()) / dist, 1.5)
          + Math.sin(Math.pow(2 * Math.PI * rawVector.abs() / dist, 2.0)));

      if (!isAwayFromEdge(curPos)) {
        vector = vector.normalize().scale(buffer);
      }

      if (!isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        sign = -1.0;
      }

      if (isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        return vector.rotate(sign * angle);
      } else {
        return vector;
      }
    } else {
      return new Vector(0, 0);
    }
  }

  @Override
  public void onHitRobot(final HitRobotEvent event) {
  }

  @Override
  public void onHitByBullet(final HitByBulletEvent event) {
  }

}