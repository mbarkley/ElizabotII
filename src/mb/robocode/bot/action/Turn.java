package mb.robocode.bot.action;

import robocode.AdvancedRobot;
import mb.robocode.api.Action;

public class Turn implements Action {
  
  private final double turnRadians;
  
  public Turn(final double turnRadians) {
    this.turnRadians = turnRadians;
  }

  @Override
  public void setAction(final AdvancedRobot robot) {
    robot.setTurnRightRadians(turnRadians);
  }

}
