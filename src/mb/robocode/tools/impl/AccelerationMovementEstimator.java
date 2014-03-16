package mb.robocode.tools.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mb.robocode.tools.api.MovementEstimator;
import mb.robocode.util.Target;
import mb.robocode.util.Vector;
import robocode.Rules;

public class AccelerationMovementEstimator implements MovementEstimator {

  /**
   * Oldest to newest.
   */
  private final Map<String, List<Target>> targetMap = new HashMap<String, List<Target>>();
  private static final int MAX_TARGET_AMT = 2;

  @Override
  public Vector estimatePosition(final Target target, final int turns) {
    final Target last = getPreviousTarget(target);
    if (last == null || last.isStale(target.time)) {
      return target.pos.add(target.vel.scale((double) turns));
    } else {
      Vector pos = target.pos;
      Vector vel = target.vel;
      final Vector accel = target.vel.minus(last.vel).scale(
          1.0 / (target.time - last.time));

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
  public void update(final Target target) {
    List<Target> list = targetMap.get(target.name);
    if (list == null) {
      list = new LinkedList<Target>();
      targetMap.put(target.name, list);
    }

    list.add(target);

    if (list.size() > MAX_TARGET_AMT) {
      list.remove(0);
    }
  }
  
  private Target getPreviousTarget(final Target curTarget) {
    Target retVal = null;
    final List<Target> list = targetMap.get(curTarget.name);

    if (list != null) {
      for (int i = list.size() - 1; i > -1; i--) {
        if (list.get(i).time < curTarget.time) {
          retVal = list.get(i);
          break;
        }
      }
      
    }

    return retVal;
  }

}
