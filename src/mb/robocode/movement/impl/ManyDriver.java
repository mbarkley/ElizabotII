package mb.robocode.movement.impl;

import java.util.Random;

import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;

public class ManyDriver extends BaseDriver {

  private final Vector[] corners;
  private final Random rand = new Random();
  private Vector destination;
  private static final double DESTINATION_DELTA = Rules.MAX_VELOCITY;
  private static final double EVASION_PERIOD = 40;

  public ManyDriver(final Vector robotBound, final Vector battleFieldBound) {
    super(battleFieldBound, robotBound);
    corners = new Vector[] {
        new Vector(buffer, buffer),
        new Vector(buffer, battleFieldBound.y - buffer),
        new Vector(battleFieldBound.x - buffer, buffer),
        new Vector(battleFieldBound.x - buffer, battleFieldBound.y
            - buffer)
    };
  }

  @Override
  public Vector movement(final Vector curPos, final Target curTarget,
      final long time) {
    decrementCooldown();

    if (destination == null
        || destination.minus(curPos).abs() < DESTINATION_DELTA) {
      destination = getDifferentRandomCorner(destination);
    }

    return destination
        .minus(curPos)
        .rotate(
            Math.min(1.0, Math.pow(
                getDistanceToClosestWall(curPos) / buffer, 4.0))
            * Math.PI / 3.0 * Math.sin(2.0 * Math.PI / EVASION_PERIOD * time))
        .normalize().scale(Rules.MAX_VELOCITY);
  }

  private void maybeChangeDestination() {
    if (isCooldownZeroed()) {
      destination = getDifferentRandomCorner(destination);
      resetCooldown();
    }
  }

  @Override
  public void onHitRobot(final HitRobotEvent event) {
    maybeChangeDestination();
  }

  private Vector getDifferentRandomCorner(final Vector corner) {
    int i;
    for (i = 0; i < corners.length; i++) {
      if (corners[i].equals(corner)) {
        break;
      }
    }

    if (i < corners.length) {
      return corners[(rand.nextInt(corners.length - 1) + i + 1)
          % corners.length];
    } else {
      return corners[rand.nextInt(corners.length)];
    }
  }

  @Override
  public void onHitByBullet(final HitByBulletEvent event) {
    maybeChangeDestination();
  }

}