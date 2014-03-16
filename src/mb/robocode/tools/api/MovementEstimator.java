package mb.robocode.tools.api;

import mb.robocode.util.Target;
import mb.robocode.util.Vector;

public interface MovementEstimator {

  /**
   * @param target
   *          The target to guess a future position of.
   * @param turns
   *          The number of turns in the future.
   * @return The estimated position of {@code target} in after {@code turns}
   *         number of turns.
   */
  public Vector estimatePosition(Target target, int turns);

  /**
   * @param target
   *          Use this to provide new movement data to an estimator.
   */
  public void update(Target target);

}
