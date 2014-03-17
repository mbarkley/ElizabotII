package mb.robocode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import mb.robocode.tools.api.MovementEstimator;
import mb.robocode.tools.impl.AccelerationMovementEstimator;
import mb.robocode.util.Target;
import mb.robocode.util.Vector;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class ElizabotII extends AdvancedRobot {

  private interface MovementDriver {
    Vector movement();

    void onHitRobot(HitRobotEvent event);
  }

  private class OneOnOneDriver implements MovementDriver {

    private final double dist;
    private final double buffer;
    private double sign = 1.0;

    public OneOnOneDriver(final double dist) {
      this.dist = dist;
      this.buffer = 1.5 * Math.sqrt(getHeight() * getHeight() + getWidth()
          * getWidth());
    }

    private boolean isAwayFromEdge(final Vector pos) {
      final Vector restrictedTopRight = new Vector(getBattleFieldWidth()
          - buffer, getBattleFieldHeight() - buffer);
      final Vector restrictedBottomLeft = new Vector(buffer, buffer);

      return pos.isBoundBy(restrictedTopRight)
          && restrictedBottomLeft.isBoundBy(pos);
    }

    @Override
    public Vector movement() {
      if (curTarget != null) {
        final Vector pos = movementEstimator.estimatePosition(curTarget,
            (int) (getTime() - curTarget.time));
        Vector myPos = new Vector(getX(), getY());
        final Vector rawVector = pos.minus(myPos);
        Vector vector = rawVector
            .normalize()
            .scale(2 * Rules.MAX_VELOCITY);

        final double angle = Math.PI / 2.0
            * (Math.pow(Math.max(0.0, dist - rawVector.abs()) / dist, 1.5)
            + Math.sin(Math.pow(2 * Math.PI * rawVector.abs() / dist, 2.0)));

        if (!isAwayFromEdge(myPos)) {
          vector = vector.normalize().scale(buffer);
        }

        if (!isAwayFromEdge(myPos.add(vector.rotate(sign * angle)))) {
          sign = -1.0;
        }

        if (isAwayFromEdge(myPos.add(vector.rotate(sign * angle)))) {
          return vector.rotate(sign * angle);
        } else {
          return vector;
        }
      } else {
        return new Vector(0, 0);
      }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
    }

  }

  private class DefensiveDriver implements MovementDriver {

    private final Vector[] corners;
    private Vector destination;

    private DefensiveDriver() {
      final double margin = 1.1 * Math.sqrt(getHeight() * getHeight()
          + getWidth() * getWidth());
      corners = new Vector[] {
          new Vector(margin, margin),
          new Vector(margin, getBattleFieldHeight() - margin),
          new Vector(getBattleFieldWidth() - margin, margin),
          new Vector(getBattleFieldWidth() - margin, getBattleFieldHeight()
              - margin)
      };
    }

    private void sortCornersByDistance(final Vector myPos) {
      Arrays.sort(corners, new Comparator<Vector>() {
        @Override
        public int compare(Vector o1, Vector o2) {
          return (int) Math.signum(o1.minus(myPos).abs()
              - o2.minus(myPos).abs());
        }
      });
    }

    private void sortCornersByAngle(final Vector myPos, final Vector myVel) {
      Arrays.sort(corners, new Comparator<Vector>() {
        private double angle(final Vector v) {
          return v.minus(myPos).angle(myVel);
        }

        @Override
        public int compare(Vector o1, Vector o2) {
          return (int) (angle(o1) - angle(o2));
        }
      });
    }

    @Override
    public Vector movement() {
      final Vector myPos = new Vector(getX(), getY());
      sortCornersByDistance(myPos);
      if (destination == null) {
        destination = corners[0];
      } else if (destination.minus(myPos).abs() < 0.1) {
        destination = corners[new Random().nextInt(2) + 1];
      }

      return destination.minus(myPos);
    }

    @Override
    public void onHitRobot(final HitRobotEvent event) {
      sortCornersByAngle(new Vector(getX(), getY()),
          Vector.polarToComponent(getHeadingRadians(), getVelocity()));
      destination = corners[3];
    }

  }

  private Target curTarget;
  private MovementEstimator movementEstimator;
  private MovementDriver driver;
  private Vector _guessAimDebug;
  private static final int DEPTH = 100;
  private static final double AIM_DELTA = 0.001;

  private Vector topRight;
  private Vector bottomLeft = new Vector(0, 0);

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  private void init() {
    setColors(Color.MAGENTA, Color.MAGENTA, Color.YELLOW, Color.BLUE, null);
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);
    topRight = new Vector(getBattleFieldWidth(), getBattleFieldHeight());
    movementEstimator = new AccelerationMovementEstimator();
    driver = (getOthers() > 2) ? new DefensiveDriver() : new OneOnOneDriver(
        Math
            .sqrt(getBattleFieldHeight()
                * getBattleFieldHeight() + getBattleFieldWidth()
                * getBattleFieldWidth()));
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

      setDrive(driver.movement());

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
    final Vector myPos = new Vector(getX(), getY());
    final long curTime = getTime();

    for (int turns = DEPTH; turns >= 0; turns--) {
      final int timeDiff = turns + (int) (curTime - target.time);
      final Vector targetPos = movementEstimator.estimatePosition(target,
          timeDiff);
      final Vector relPos = targetPos.minus(myPos);
      final double bulletSpeed = relPos.abs() / ((double) turns - 1);
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
    return pos.isBoundBy(topRight) && bottomLeft.isBoundBy(pos);
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

        if (_guessAimDebug != null) {
          fillSquareAt(_guessAimDebug, 8, g);
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
      driver = new OneOnOneDriver(Math.sqrt(getBattleFieldHeight()
          * getBattleFieldHeight() + getBattleFieldWidth()
          * getBattleFieldWidth()));
    }
  }

  private double scoreTarget(final Target target) {
    final double timeScore = Math.pow(0.95, getTime() - target.time);
    final double posScore = 300.0 / (target.pos
        .minus(new Vector(getX(), getY())).abs());

    return posScore * timeScore;
  }

}
