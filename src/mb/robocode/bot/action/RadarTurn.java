package mb.robocode.bot.action;

import robocode.AdvancedRobot;
import mb.robocode.api.Action;

public class RadarTurn implements Action {
  
  private final double turnRightRadians;

  public RadarTurn(final double turnRightRadians) {
    this.turnRightRadians = turnRightRadians;
  }

  @Override
  public void setAction(final AdvancedRobot robot) {
    robot.setTurnRadarRightRadians(turnRightRadians);
  }

}
