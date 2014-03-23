package mb.robocode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import mb.robocode.gun.GunTargeter;
import mb.robocode.gun.impl.FirstMatchTargeter;
import mb.robocode.iter.IntegerIterable;
import mb.robocode.movement.MovementDriver;
import mb.robocode.movement.MovementEstimator;
import mb.robocode.movement.impl.AccelerationMovementEstimator;
import mb.robocode.movement.impl.FewDriver;
import mb.robocode.movement.impl.ManyDriver;
import mb.robocode.vector.Target;
import mb.robocode.vector.Vector;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class ElizabotII extends AdvancedRobot {

  private Target curTarget;
  private MovementEstimator movementEstimator;
  private GunTargeter gunTargeter;
  private MovementDriver driver;
  private static final int DEPTH = 100;
  private static final double AIM_DELTA = 0.001;

  private Vector boardTopRight;

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  private void init() {
    setColors(Color.MAGENTA, Color.MAGENTA, Color.YELLOW, Color.BLUE, null);
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);
    boardTopRight = new Vector(getBattleFieldWidth(), getBattleFieldHeight());
    movementEstimator = new AccelerationMovementEstimator();
    if (getOthers() > 2) {
      gunTargeter = new FirstMatchTargeter(new IntegerIterable(0, DEPTH + 1),
          boardTopRight);
      driver = getManyDriver();
    } else {
      gunTargeter = new FirstMatchTargeter(new IntegerIterable(DEPTH, -1, -1),
          boardTopRight);
      driver = getFewDriver();
    }
  }

  public ManyDriver getManyDriver() {
    return new ManyDriver(new Vector(getWidth(), getHeight()), new Vector(
        getBattleFieldWidth(), getBattleFieldHeight()));
  }

  public FewDriver getFewDriver() {
    return new FewDriver(
        Math
            .sqrt(getBattleFieldHeight()
                * getBattleFieldHeight() + getBattleFieldWidth()
                * getBattleFieldWidth()),
        new Vector(getBattleFieldWidth(), getBattleFieldHeight()),
        new Vector(getWidth(), getHeight()),
        movementEstimator);
  }

  @Override
  public void run() {
    init();

    while (true) {
      if (curTarget != null && curTarget.isStale(getTime())) {
        curTarget = null;
      }

      if (curTarget == null) {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
      } else {
        final Vector targetPos = movementEstimator.estimatePosition(curTarget,
            (int) (getTime() - curTarget.time));
        final Vector relPos = targetPos.minus(new Vector(getX(), getY()));
        final Vector radarVector = Vector.polarToComponent(
            getRadarHeadingRadians(), 1.0);

        setTurnRadarRightRadians(getRotationAngle(radarVector, relPos));
      }

      if (curTarget != null) {
        final double firePower = aimAtTarget(curTarget);
        if (firePower != 0.0 && getGunTurnRemainingRadians() < AIM_DELTA
            && getGunHeat() == 0.0) {
          setFire(firePower);
        }
      }

      setDrive(driver
          .movement(new Vector(getX(), getY()), curTarget, getTime()));

      execute();
    }
  }

  private void setDrive(final Vector vector) {
    if (vector.abs() == 0.0) {
      setAhead(0.0);
      setTurnRightRadians(0.0);
    } else {
      final Vector headingVector = Vector.polarToComponent(getHeadingRadians(),
          1.0);
      final double rawAngle = vector.angle(headingVector);
      final double sign = (rawAngle > Math.PI / 2.0) ? -1.0 : 1.0;
      double turnAngle = getRotationAngle(headingVector, vector);
      if (sign < 0.0) {
        turnAngle = -Math.signum(turnAngle) * (Math.PI - rawAngle);
      }

      setAhead(sign * vector.abs());
      setTurnRightRadians(turnAngle);
    }
  }

  /**
   * Direct gun to aim at given target. Returns the firepower (0 is firing
   * shouldn't happen). towards the target.
   */
  private double aimAtTarget(final Target target) {
    assert target != null;
    final Vector gunVector = Vector.polarToComponent(getGunHeadingRadians(),
        1.0);
    Vector aimVector = guessAimVector(target);
    final double result;

    if (aimVector.abs() > 0) {
      result = aimVector.abs();
    } else {
      result = 0.0;
      aimVector = target.pos.minus(new Vector(getX(), getY()));
    }

    setTurnGunRightRadians(getRotationAngle(gunVector, aimVector));

    return result;
  }

  private double getRotationAngle(final Vector deviceVector,
      final Vector targetVector) {
    final double angle = deviceVector.angle(targetVector);

    final double rotateRight = deviceVector.rotate(angle).angle(targetVector);
    final double rotateLeft = deviceVector.rotate(-angle).angle(targetVector);
    if (rotateRight <= rotateLeft) {
      return angle;
    }
    else {
      return -angle;
    }
  }

  /**
   * Get a relative vector from our position to the spot to aim. The maginitude
   * represents the bullet power.
   */
  private Vector guessAimVector(final Target target) {
    return gunTargeter.getAimVector(target, new Vector(getX(), getY()),
        getTime(), movementEstimator);
  }

  @Override
  public void onPaint(Graphics2D g) {
    if (curTarget != null) {
      g.setColor(Color.YELLOW);
      final long time = getTime();
      for (int turns = 0; turns <= DEPTH; turns++) {
        final Vector pos = movementEstimator.estimatePosition(curTarget,
            (int) (turns + (time - curTarget.time)));
        fillSquareAt(pos, 4, g);
      }

      Vector aimAt = guessAimVector(curTarget);
      final double power = aimAt.abs();
      final double speed = Rules.getBulletSpeed(power);
      if (power > 0) {
        g.setColor(Color.RED);
        aimAt = aimAt.normalize().scale(speed);
        Vector curPos = new Vector(getX(), getY()).add(aimAt);
        final Vector upperBound = new Vector(getBattleFieldWidth(),
            getBattleFieldHeight());
        final Vector lowerBound = new Vector(0, 0);

        while (curPos.isBoundBy(upperBound) && lowerBound.isBoundBy(curPos)) {
          fillSquareAt(curPos, 4, g);
          curPos = curPos.add(aimAt);
        }

        if (gunTargeter.getLastAimVector() != null) {
          fillSquareAt(gunTargeter.getLastAimVector(), 8, g);
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
      curTarget = target;
    }

    movementEstimator.update(target);
  }

  @Override
  public void onHitRobot(final HitRobotEvent event) {
    driver.onHitRobot(event);
    if (curTarget == null
        || curTarget.pos.minus(new Vector(getX(), getY())).abs() > 20) {
      curTarget = new Target(event.getName(), Vector.polarToComponent(
          getHeadingRadians() + event.getBearingRadians(), getHeight()).add(
          new Vector(getX(), getY())), new Vector(0, 0), event.getTime());
    }
  }

  @Override
  public void onRobotDeath(final RobotDeathEvent event) {
    if (curTarget != null && event.getName().equals(curTarget.name)) {
      curTarget = null;
    }

    if (getOthers() <= 2) {
      gunTargeter = new FirstMatchTargeter(new IntegerIterable(DEPTH, -1, -1),
          boardTopRight);
      driver = getFewDriver();
    }
  }

  private double scoreTarget(final Target target) {
    final double timeScore = Math.pow(0.95, getTime() - target.time);
    final double posScore = 300.0 / (target.pos
        .minus(new Vector(getX(), getY())).abs());

    return posScore * timeScore;
  }

}
