package mb.robocode.bot;

import static mb.robocode.util.Logger.debug;

import java.awt.Color;

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
      debug("main loop start");

      setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
      debug("Set radar turn right positive infinity");

      if (curTarget != null) {
        debug("Current target pos: " + curTarget.pos);
        aimAtTarget(curTarget);
        if (getGunTurnRemainingRadians() < Math.PI / 256.0
            && getGunHeat() == 0.0) {
          debug("Firing gun");
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

      debug("Executing actions");
      execute();
    }
  }

  private void aimAtTarget(final Target target) {
    debug("Starting aimAtTarget");
    assert target != null;
    debug(String.format("Gun Heading Radians: %.2f", getGunHeadingRadians()));
    final Vector gunVector = Vector.polarToComponent(getGunHeadingRadians(),
        1.0);
    debug("Gun Vector: " + gunVector);
    final Vector aimVector = guessAimVector(target);
    final double gunAngle = gunVector.angle(aimVector);
    debug(String.format("Gun angle: %.2f", gunAngle));

    final double rotateRight = gunVector.rotate(gunAngle).angle(aimVector);
    final double rotateLeft = gunVector.rotate(-gunAngle).angle(aimVector);
    debug("rotateRight diff: " + Double.toString(rotateRight));
    debug("rotateLeft diff: " + Double.toString(rotateLeft));
    if (rotateRight <= rotateLeft) {
      setTurnGunRightRadians(gunAngle);
    }
    else {
      setTurnGunLeftRadians(gunAngle);
    }
    debug("Finished aimAtTarget");
  }

  private Vector guessAimVector(final Target target) {
    final int DEPTH = 20;
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
        return relPos.scale(bulletPower / relPos.abs());
      }
    }

    return target.pos.add(target.vel.scale(DEPTH + 1)).minus(myPos).normalize()
        .scale(Rules.MAX_BULLET_POWER);
  }

  private Vector getFuturePositionEstimate(final Target target, final int turns) {
    return target.pos.add(target.vel.scale((double) turns));
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
