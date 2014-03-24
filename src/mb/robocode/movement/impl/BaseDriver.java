package mb.robocode.movement.impl;

import mb.robocode.movement.MovementDriver;
import mb.robocode.vector.Vector;

public abstract class BaseDriver implements MovementDriver {

  protected final double buffer;
  protected final Vector battleFieldBound;
  protected final Vector restrictedTopRight;
  protected final Vector restrictedBottomLeft;

  protected BaseDriver(final Vector battleFieldBound, final Vector robotBound) {
    this.battleFieldBound = battleFieldBound;
    this.buffer = 1.5 * Math.sqrt(robotBound.y * robotBound.y + robotBound.x
        * robotBound.x);
    restrictedTopRight = new Vector(battleFieldBound.x
        - buffer, battleFieldBound.y - buffer);
    restrictedBottomLeft = new Vector(buffer, buffer);
  }

  protected boolean isAwayFromEdge(final Vector pos) {
    return pos.isBoundBy(restrictedTopRight)
        && restrictedBottomLeft.isBoundBy(pos);
  }

  protected double getDistanceToClosestWall(final Vector pos) {
    final Vector[] walls = new Vector[] {
        new Vector(pos.x, 0.0),
        new Vector(pos.x, battleFieldBound.y),
        new Vector(0.0, pos.y),
        new Vector(battleFieldBound.x, pos.y)
    };

    Vector closestVector = walls[0];
    for (int i = 1; i < walls.length; i++) {
      if (walls[i].minus(pos).abs() < closestVector.minus(pos).abs()) {
        closestVector = walls[i];
      }
    }

    return closestVector.minus(pos).abs();
  }

}
