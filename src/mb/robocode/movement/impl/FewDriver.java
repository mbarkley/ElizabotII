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

      /*
       * This expression causes movement towards an opponent, around an opponent
       * depending on distance.
       * 
       * The first term is close to 0.0 when we are far away from an opponent
       * and approaches 90-degrees when close. This causes us to approach an
       * opponent when far away, but maneuver around when close.
       * 
       * The second term is sinusoidal with a large period (relative to the size
       * of the game board). The effect of this term is that: when far away we
       * will directly approach our enemy; in mid range we will zig-zag; and in
       * close range this term will approach zero and the first term will become
       * dominant.
       */
      final double angle = Math.PI / 2.0
          * (Math.pow(Math.max(0.0, dist - rawVector.abs()) / dist, 1.5)
          + Math.sin(Math.pow(2 * Math.PI * rawVector.abs() / dist, 2.0)));

      if (!isAwayFromEdge(curPos)) {
        /*
         * Scale the movement vector if we are close to the edge to accurately
         * assess whether we are in danger of running into a wall.
         */
        vector = vector.normalize().scale(buffer);
      }

      if (!isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        // Maybe we can try turning in the other direction...
        sign = -1.0;
      }

      if (isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        return vector.rotate(sign * angle);
      } else {
        /*
         * If we're really close to the edge, just follow our opponent (since he
         * may never be passed the edge of the wall).
         */
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