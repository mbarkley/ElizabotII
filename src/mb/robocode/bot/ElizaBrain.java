package mb.robocode.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mb.robocode.api.Action;
import mb.robocode.api.Brain;
import mb.robocode.api.EventMap;
import mb.robocode.bot.action.Ahead;
import mb.robocode.bot.action.RadarTurn;
import mb.robocode.bot.action.Turn;
import mb.robocode.bot.strategy.ComplexStrategy;
import mb.robocode.bot.strategy.SimpleStrategy;
import mb.robocode.bot.strategy.Strategy;
import robocode.AdvancedRobot;
import robocode.Event;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class ElizaBrain implements Brain {

  private static final double RANGE_INCREMENT = 100.0;
  private double WIDTH;
  private double HEIGHT;

  @Override
  public void init(final AdvancedRobot robot) {
    WIDTH = robot.getWidth();
    HEIGHT = robot.getHeight();
  }

  @Override
  public Collection<Action> getActions(final List<EventMap> events) {
    final Collection<Action> retVal = new ArrayList<Action>();

    final Strategy strategy = getStrategy(events);
    retVal.addAll(strategy.getActions());

    return retVal;
  }

  private Collection<Action> moveToAction(final ScannedRobotEvent target,
      final StatusEvent status) {
    final Collection<Action> retVal = new ArrayList<Action>(2);
    retVal.add(new Ahead(target.getDistance()));
    retVal.add(new Turn(target.getBearingRadians()));

    return retVal;
  }

  private Strategy getStrategy(final List<EventMap> events) {
    final Collection<Strategy> subStrategies = Arrays.asList(new Strategy[] {
        getMovementStrategy(events),
        getGunStrategy(events),
        getRadarStrategy(events)
    });

    return new ComplexStrategy(subStrategies);
  }

  private Strategy getRadarStrategy(final List<EventMap> events) {
    return new SimpleStrategy(Arrays.asList(new Action[] {
        new RadarTurn(Double.POSITIVE_INFINITY)
    }));
  }

  private Strategy getGunStrategy(final List<EventMap> events) {
    final List<Action> actions = new ArrayList<Action>();
    if (events.size() > 0) {
      final StatusEvent status = events.get(0).safeGet(StatusEvent.class)
          .iterator().next();
      final List<ScannedRobotEvent> scans = getMostRecentScans(events);

      Collections.sort(scans, new Comparator<ScannedRobotEvent>() {
        private double getAbsBearingFromGun(final ScannedRobotEvent scan) {
          return Math.abs(Utils.normalRelativeAngle(
              scan.getHeadingRadians()
                  - status.getStatus().getGunHeadingRadians()));
        }

        @Override
        public int compare(ScannedRobotEvent o1, ScannedRobotEvent o2) {
          return (int) (getAbsBearingFromGun(o2) - getAbsBearingFromGun(o1));
        }
      });

      if (scans.size() > 0) {
        final ScannedRobotEvent target = scans.get(0);
        final double HIT_RATIO = 10.0;
        final double TARGET_WINDOW = (HIT_RATIO * Math.min(HEIGHT, WIDTH)) / 2.0;
        final double distance = target.getDistance();
        final double bearing = Utils.normalRelativeAngle(status.getStatus()
            .getHeadingRadians() + target.getBearingRadians()
            - status.getStatus().getGunHeadingRadians());
        final double absBearing = Math.abs(bearing);

        if (absBearing < Math.PI / 2.0
            && Math.sin(absBearing) < TARGET_WINDOW
                / (TARGET_WINDOW * TARGET_WINDOW + distance * distance)) {
          actions.add(new Action() {
            @Override
            public void setAction(final AdvancedRobot robot) {
              if (robot.getGunHeat() == 0.0)
                robot.setFire(2.0);
              else
                robot.setTurnGunRightRadians(bearing);
            }
          });
        }
        else {
          actions.add(new Action() {
            @Override
            public void setAction(final AdvancedRobot robot) {
              robot.setTurnGunRightRadians(bearing);
            }
          });
        }
      }
    }

    return new SimpleStrategy(actions);
  }

  private List<ScannedRobotEvent> getMostRecentScans(final List<EventMap> events) {
    final Map<String, ScannedRobotEvent> mostRecent = new HashMap<String, ScannedRobotEvent>();
    for (final EventMap map : events) {
      for (final ScannedRobotEvent scanEvent : map
          .safeGet(ScannedRobotEvent.class)) {
        if (!mostRecent.containsKey(scanEvent.getName())) {
          mostRecent.put(scanEvent.getName(), scanEvent);
        }
      }
    }

    return new ArrayList<ScannedRobotEvent>(mostRecent.values());
  }

  private Strategy getMovementStrategy(final List<EventMap> events) {
    final Map<String, ScannedRobotEvent> latestScanEvents = getLatestScanEvents(events);
    final double positionScore = getPositionScore(latestScanEvents);

    return new SimpleStrategy(Arrays.asList(new Action[] {

        }));
  }

  private double getPositionScore(Map<String, ScannedRobotEvent> latestScans) {
    double score = 0.0;
    for (final Entry<String, ScannedRobotEvent> entry : latestScans.entrySet()) {
      final double enemyDistance = entry.getValue().getDistance();
      final double rangeMod = 1.0 / (enemyDistance / RANGE_INCREMENT + 1);
      score += rangeMod;
    }

    return score;
  }

  private Map<String, ScannedRobotEvent> getLatestScanEvents(
      final List<EventMap> events) {
    final Map<String, ScannedRobotEvent> retVal = new HashMap<String, ScannedRobotEvent>();

    for (final EventMap eventMap : events) {
      for (final ScannedRobotEvent scanEvent : eventMap
          .safeGet(ScannedRobotEvent.class)) {
        if (!retVal.containsKey(scanEvent.getName())) {
          retVal.put(scanEvent.getName(), scanEvent);
        }
      }
    }

    return retVal;
  }

}
