package mb.robocode.movement.impl;

import java.util.Random;

import mb.robocode.movement.MovementDriver;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.HitRobotEvent;

public class ManyDriver implements MovementDriver {

  private final Vector[] corners;
  private final Random rand = new Random();
  private Vector destination;

  public ManyDriver(final Vector robotBound, final Vector battleFieldBound) {
    final double margin = 1.1 * Math.sqrt(robotBound.x * robotBound.x
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
    if (destination == null || destination.minus(curPos).abs() < 0.1) {
      destination = getDifferentRandomCorner(destination);
    }

    return destination.minus(curPos);
  }

  @Override
  public void onHitRobot(final HitRobotEvent event) {
    destination = getDifferentRandomCorner(destination);
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

}