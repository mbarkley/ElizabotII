package mb.robocode.movement.impl;

import mb.robocode.movement.MovementEstimator;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;

public class FewDriver extends BaseDriver {

  private final double dist;
  private double sign = 1.0;

  private final MovementEstimator movementEstimator;

  public FewDriver(final double dist, final Vector battleFieldBound,
      final Vector robotBound, final MovementEstimator movementEstimator) {
    super(battleFieldBound, robotBound);
    this.dist = dist;
    this.movementEstimator = movementEstimator;
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

      vector = vector.normalize().scale(Rules.MAX_VELOCITY);

      if (!isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        // Maybe we can try turning in the other direction...
        sign = -1.0;
      }

      if (isAwayFromEdge(curPos.add(vector.rotate(sign * angle)))) {
        return vector.rotate(sign * angle);
      } else {
        final double wallDistanceRatio = getDistanceToClosestWall(curPos)
            / buffer;
        /*
         * In this case we're really close to a corner. The edgeCoefficient
         * causes us to sharply straighten course towards our opponent (which
         * will necessarily not be towards a wall) but in a smooth way that does
         * not cause jerky boundary behaviour.
         */
        final double edgeCoefficient = Math.pow(wallDistanceRatio, 4.0);
        return vector.rotate(sign * angle * edgeCoefficient).scale(
            Math.sqrt(wallDistanceRatio));
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