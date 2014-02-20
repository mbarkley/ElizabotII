package mb.robocode.bot;

import static mb.robocode.util.Logger.debug;

import java.util.ArrayList;
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
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class ElizaBrain implements Brain {

  private static final double RANGE_INCREMENT = 100.0;

  @Override
  public Collection<Action> getActions(final List<EventMap> events) {
    final Collection<Action> retVal = new ArrayList<Action>();
    retVal.add(new RadarTurn(Double.POSITIVE_INFINITY));

    final Map<String, ScannedRobotEvent> latestScans = getLatestScanEvents(events);
    final List<ScannedRobotEvent> scanList = new ArrayList<ScannedRobotEvent>(
        latestScans.values());

    Collections.sort(scanList, new Comparator<ScannedRobotEvent>() {
      @Override
      public int compare(ScannedRobotEvent o1, ScannedRobotEvent o2) {
        return (int) Math.signum(o1.getDistance() - o2.getDistance());
      }
    });

    if (!scanList.isEmpty()) {
      final ScannedRobotEvent closest = scanList.get(0);

      switch (getStrategy(events)) {
        case HOLD:
          debug("Strategy: " + Strategy.HOLD.toString());
          break;
        case ATTACK:
          debug("Strategy: " + Strategy.ATTACK.toString());
          retVal.addAll(moveToAction(closest,
              events.get(0).safeGet(StatusEvent.class).iterator().next()));
          break;
        case EVADE:
          debug("Strategy: " + Strategy.EVADE.toString());
          break;
      }
    }

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
    final Map<String, ScannedRobotEvent> latestScans = getLatestScanEvents(events);
    final double score = getPositionScore(latestScans);

    if (score < 1.0) {
      return Strategy.ATTACK;
    } else if (score < 2.0) {
      return Strategy.HOLD;
    } else {
      return Strategy.EVADE;
    }
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
