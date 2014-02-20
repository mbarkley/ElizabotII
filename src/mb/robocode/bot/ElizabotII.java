package mb.robocode.bot;

import java.awt.Color;

import mb.robocode.api.BaseRobot;
import mb.robocode.api.Brain;

public class ElizabotII extends BaseRobot {

  @Override
  protected Brain makeBrain() {
    return new ElizaBrain();
  }

  @Override
  protected void init() {
    setBodyColor(Color.MAGENTA);
    setRadarColor(Color.MAGENTA);
    setGunColor(Color.MAGENTA);
    setBulletColor(Color.BLUE);
  }

  @Override
  protected int getEventContextSize() {
    return 10;
  }

}
