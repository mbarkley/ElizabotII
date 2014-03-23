package mb.robocode.movement.impl;

import java.util.Random;

import mb.robocode.movement.MovementDriver;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;

public class ManyDriver implements MovementDriver {

  private final Vector[] corners;
  private final Random rand = new Random();
  private Vector destination;
  private int cooldown = 0;
  private static final int MAX_COOLDOWN = 10;
  private static final double DESTINATION_DELTA = 1.0;

  public ManyDriver(final Vector robotBound, final Vector battleFieldBound) {
    final double margin = 1.5 * Math.sqrt(robotBound.x * robotBound.x
        + robotBound.y * robotBound.y);
    corners = new Vector[] {
        new Vector(margin, margin),
        new Vector(margin, battleFieldBound.y - margin),
        new Vector(battleFieldBound.x - margin, margin),
        new Vector(battleFieldBound.x - margin, battleFieldBound.y
            - margin)
    };
  }

  @Override
  public Vector movement(final Vector curPos, final Target curTarget,
      final long time) {
    cooldown = Math.max(cooldown - 1, 0);

    if (destination == null
        || destination.minus(curPos).abs() < DESTINATION_DELTA) {
      destination = getDifferentRandomCorner(destination);
    }

    return destination.minus(curPos).normalize().scale(Rules.MAX_VELOCITY);
  }

  private void maybeChangeDestination() {
    if (cooldown == 0) {
      destination = getDifferentRandomCorner(destination);
      cooldown = MAX_COOLDOWN;
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