package mb.robocode.bot;

import static mb.robocode.util.Logger.debug;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import mb.robocode.util.Vector;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ElizabotII extends AdvancedRobot {

  private class Target {
    public final Vector pos;
    public final Vector vel;
    public final long time;

    private Target(final Vector pos, final Vector vel, final long time) {
      this.pos = pos;
      this.vel = vel;
      this.time = time;
    }
  }

  private Target curTarget;
  private static final int DEPTH = 20;

  private void init() {
    setColors(Color.MAGENTA, Color.MAGENTA, Color.YELLOW, Color.BLUE, null);
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);
  }

  @Override
  public void run() {
    init();

    while (true) {
      setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

      if (curTarget != null) {
        aimAtTarget(curTarget);
        if (getGunTurnRemainingRadians() < Math.PI / 256.0
            && getGunHeat() == 0.0) {
          setFire(2.0);
        }

        Vector vel = Vector.polarToComponent(getHeadingRadians(),
            (getVelocity() > 0.0) ? getVelocity() : 1.0);
        Vector displacement = Vector.polarToComponent(
            vel
                .rotate(Math.PI / 2.0).heading(), 100.0);
        if (!curTarget.pos.add(displacement).isBoundBy(
            new Vector(getBattleFieldWidth(), getBattleFieldHeight()))) {
          displacement.rotate(Math.PI);
        }

        final Vector move = curTarget.pos.add(displacement).minus(
            new Vector(getX(), getY()));
        setAhead(move.abs());
        setTurnRightRadians(Utils.normalRelativeAngle(move.heading()
            - getHeadingRadians()));
      }

      execute();
    }
  }

  private void aimAtTarget(final Target target) {
    debug("Starting aimAtTarget");
    assert target != null;
    final Vector gunVector = Vector.polarToComponent(getGunHeadingRadians(),
        1.0);
    final Vector aimVector = guessAimVector(target);
    final double gunAngle = gunVector.angle(aimVector);

    final double rotateRight = gunVector.rotate(gunAngle).angle(aimVector);
    final double rotateLeft = gunVector.rotate(-gunAngle).angle(aimVector);
    if (rotateRight <= rotateLeft) {
      setTurnGunRightRadians(gunAngle);
    }
    else {
      setTurnGunLeftRadians(gunAngle);
    }
  }

  private Vector guessAimVector(final Target target) {
    final Vector myPos = new Vector(getX(), getY());
    final long curTime = getTime();

    for (int turns = 0; turns <= DEPTH; turns++) {
      final int timeDiff = turns + (int) (curTime - target.time);
      final Vector targetPos = getFuturePositionEstimate(target, timeDiff);
      final Vector relPos = targetPos.minus(myPos);
      final double bulletSpeed = relPos.abs() / ((double) timeDiff);
      final double bulletPower = (20.0 - bulletSpeed) / 3.0;

      if (bulletPower <= Rules.MAX_BULLET_POWER
          && bulletPower >= Rules.MIN_BULLET_POWER) {
        return relPos.normalize().scale(bulletPower);
      }
    }

    return new Vector(0, 0);
  }

  /**
   * Get position vector for a target for a number of turns in the future.
   */
  private Vector getFuturePositionEstimate(final Target target, final int turns) {
    return target.pos.add(target.vel.scale((double) turns));
  }

  @Override
  public void onPaint(Graphics2D g) {
    if (curTarget != null) {
      g.setColor(Color.YELLOW);
      final long time = getTime();
      for (int turns = 0; turns <= DEPTH; turns++) {
        final Vector pos = getFuturePositionEstimate(curTarget,
            (int) (turns + (time - curTarget.time)));
        fillSquareAt(pos, 4, g);
      }

      Vector aimAt = guessAimVector(curTarget);
      if (aimAt.abs() > 0) {
        g.setColor(Color.RED);
        aimAt = aimAt.normalize().scale(Rules.getBulletSpeed(aimAt.abs()));
        Vector curPos = new Vector(getX(), getY()).add(aimAt);
        final Vector upperBound = new Vector(getBattleFieldWidth(), getBattleFieldHeight());
        final Vector lowerBound = new Vector(0,0);

        while (curPos.isBoundBy(upperBound) && lowerBound.isBoundBy(curPos)) {
          fillSquareAt(curPos, 4, g);
          curPos = curPos.add(aimAt);
        }
      }
    }
  }

  private void fillSquareAt(final Vector pos, final int size, final Graphics2D g) {
    fillSquareAt(pos.x, pos.y, size, g);
  }

  private void fillSquareAt(final double x, final double y, final int size,
      Graphics2D g) {
    fillSquareAt((int) x, (int) y, size, g);
  }

  private void fillSquareAt(final int x, final int y, final int size,
      Graphics2D g) {
    g.fill(new Rectangle(x - size / 2, y - size / 2, size, size));
  }

  @Override
  public void onScannedRobot(final ScannedRobotEvent event) {
    debug("ScannedRobotEvent");
    debug(String.format("Scan Heading: %.2f",
        getHeadingRadians() + event.getBearingRadians()));
    debug(String.format("Scan Distance: %.2f", event.getDistance()));

    final Vector enemyPos = Vector.polarToComponent(
        getHeadingRadians() + event.getBearingRadians(),
        event.getDistance()).add(new Vector(getX(), getY()));

    debug(String.format("Pos vector: %s", enemyPos));

    final Vector enemyVel = Vector.polarToComponent(event.getHeadingRadians(),
        event.getVelocity());
    final Target target = new Target(enemyPos, enemyVel, getTime());

    if (curTarget != null)
      debug(String.format("curTarget score: %.2f", scoreTarget(curTarget)));
    else
      debug("curTarget is null");

    debug(String.format("newTarget score: %.2f", scoreTarget(target)));

    if (curTarget == null || scoreTarget(curTarget) < scoreTarget(target)) {
      debug("setting new target");
      curTarget = target;
    }
  }

  private double scoreTarget(final Target target) {
    final double timeScore = Math.pow(0.95, getTime() - target.time);
    final double posScore = 300.0 / (target.pos
        .minus(new Vector(getX(), getY())).abs());

    return posScore * timeScore;
  }

}
