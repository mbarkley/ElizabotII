package mb.robocode.bot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import mb.robocode.util.Vector;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ElizabotII extends AdvancedRobot {

  private class Target {
    public final String name;
    public final Vector pos;
    public final Vector vel;
    public final long time;

    private Target(final String name, final Vector pos, final Vector vel,
        final long time) {
      this.name = name;
      this.pos = pos;
      this.vel = vel;
      this.time = time;
    }
  }

  private Target curTarget;
  private Map<String, Target> recentTargets = new HashMap<String, Target>();
  private static final int DEPTH = 20;
  private static final double AIM_DELTA = 0.0000000001;

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
        final boolean result = aimAtTarget(curTarget);
        if (result && getGunTurnRemainingRadians() < AIM_DELTA
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

  /**
   * Direct gun to aim at given target. Return true if the gun is actively aimed
   * towards the target.
   */
  private boolean aimAtTarget(final Target target) {
    assert target != null;
    final Vector gunVector = Vector.polarToComponent(getGunHeadingRadians(),
        1.0);
    Vector aimVector = guessAimVector(target);
    final boolean result;

    if (aimVector.abs() > 0) {
      result = true;
    } else {
      result = false;
      aimVector = target.pos.minus(new Vector(getX(), getY()));
    }

    final double gunAngle = gunVector.angle(aimVector);

    final double rotateRight = gunVector.rotate(gunAngle).angle(aimVector);
    final double rotateLeft = gunVector.rotate(-gunAngle).angle(aimVector);
    if (rotateRight <= rotateLeft) {
      setTurnGunRightRadians(gunAngle);
    }
    else {
      setTurnGunLeftRadians(gunAngle);
    }

    return result;
  }

  /**
   * Get a relative vector from our position to the spot to aim. The maginitude
   * represents the bullet power.
   */
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
    final Target last = recentTargets.get(target.name);
    if (last == null) {
      return target.pos.add(target.vel.scale((double) turns));
    } else {
      Vector pos = target.pos;
      Vector vel = target.vel;
      final Vector accel = target.vel.minus(last.vel).scale(1.0 / (target.time - last.time));
      
      for (int t = 1; t < turns; t++) {
        vel = vel.add(accel);
        if (vel.abs() > Rules.MAX_VELOCITY) {
          vel = vel.normalize().scale(Rules.MAX_VELOCITY);
        }
        pos = pos.add(vel);
      }
      
      return pos;
    }
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
        final Vector upperBound = new Vector(getBattleFieldWidth(),
            getBattleFieldHeight());
        final Vector lowerBound = new Vector(0, 0);

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
    final Vector enemyPos = Vector.polarToComponent(
        getHeadingRadians() + event.getBearingRadians(),
        event.getDistance()).add(new Vector(getX(), getY()));

    final Vector enemyVel = Vector.polarToComponent(event.getHeadingRadians(),
        event.getVelocity());
    final Target target = new Target(event.getName(), enemyPos, enemyVel,
        getTime());

    if (curTarget == null || scoreTarget(curTarget) < scoreTarget(target)) {
      if (curTarget != null) {
        recentTargets.put(curTarget.name, curTarget);
      }
      curTarget = target;
    } else {
      recentTargets.put(target.name, target);
    }
  }

  private double scoreTarget(final Target target) {
    final double timeScore = Math.pow(0.95, getTime() - target.time);
    final double posScore = 300.0 / (target.pos
        .minus(new Vector(getX(), getY())).abs());

    return posScore * timeScore;
  }

}
