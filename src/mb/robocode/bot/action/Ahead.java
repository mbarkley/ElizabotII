package mb.robocode.bot.action;

import robocode.AdvancedRobot;
import mb.robocode.api.Action;

public class Ahead implements Action {
  
  private final double distance;
  
  public Ahead(final double distance) {
    this.distance = distance;
  }

  @Override
  public void setAction(final AdvancedRobot robot) {
    robot.setAhead(distance);
  }

}
