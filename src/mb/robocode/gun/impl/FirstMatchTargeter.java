package mb.robocode.gun.impl;

import mb.robocode.gun.GunTargeter;
import mb.robocode.movement.MovementEstimator;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.Rules;

public class FirstMatchTargeter implements GunTargeter {

  private final Iterable<Integer> turns;
  private final Vector boardBottomLeft = new Vector(0, 0);
  private final Vector boardTopRight;
  // For debugging
  private Vector _guessAimDebug;

  public FirstMatchTargeter(final Iterable<Integer> turns,
      final Vector battleFieldBound) {
    this.turns = turns;
    this.boardTopRight = battleFieldBound;
  }

  @Override
  public Vector getAimVector(final Target target, final Vector curPos,
      final long time,
      final MovementEstimator movementEstimator) {

    for (final Integer turn : turns) {
      final int timeDiff = turn + (int) (time - target.time);
      final Vector targetPos = movementEstimator.estimatePosition(target,
          timeDiff);
      final Vector relPos = targetPos.minus(curPos);
      final double bulletSpeed = relPos.abs() / ((double) turn - 1);
      final double bulletPower = (20.0 - bulletSpeed) / 3.0;

      if (bulletPower <= Rules.MAX_BULLET_POWER
          && bulletPower >= Rules.MIN_BULLET_POWER && isOnBoard(targetPos)) {
        _guessAimDebug = targetPos;
        return relPos.normalize().scale(bulletPower);
      }
    }

    _guessAimDebug = null;
    return new Vector(0, 0);
  }

  private boolean isOnBoard(final Vector pos) {
    return pos.isBoundBy(boardTopRight) && boardBottomLeft.isBoundBy(pos);
  }

  @Override
  public Vector getLastAimVector() {
    return _guessAimDebug;
  }

}