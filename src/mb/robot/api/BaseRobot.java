package mb.robot.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import mb.robot.impl.EventMapImpl;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.Event;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;

public abstract class BaseRobot extends AdvancedRobot {

  private final Brain brain = makeBrain();
  private final int contextSize = Math.max(1, getEventContextSize());
  private final List<EventMap> context = new ArrayList<EventMap>(contextSize);
  private EventMap curEvents = getNewEventMap();

  protected abstract Brain makeBrain();

  protected abstract void init();

  protected abstract int getEventContextSize();

  @Override
  public void run() {
    init();
    privateInit();

    while (true) {
      if (context.size() == contextSize)
        context.remove(contextSize - 1);
      context.add(0, curEvents);
      curEvents = getNewEventMap();

      final Collection<Action> actions = brain.getActions(context);
      for (final Action action : actions) {
        action.performAction(this);
      }
    }
  }

  private EventMap getNewEventMap() {
    return new EventMapImpl(
        new HashMap<Class<? extends Event>, Collection<Event>>());
  }

  private void privateInit() {
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);
  }

  private <E extends Event> void onEvent(E event) {
    curEvents.get(event.getClass()).add(event);
  }

  @Override
  public void onDeath(DeathEvent event) {
    onEvent(event);
  }

  @Override
  public void onSkippedTurn(SkippedTurnEvent event) {
    onEvent(event);
  }

  @Override
  public void onBattleEnded(BattleEndedEvent event) {
    onEvent(event);
  }

  @Override
  public void onBulletHit(BulletHitEvent event) {
    onEvent(event);
  }

  @Override
  public void onBulletHitBullet(BulletHitBulletEvent event) {
    onEvent(event);
  }

  @Override
  public void onBulletMissed(BulletMissedEvent event) {
    onEvent(event);
  }

  @Override
  public void onHitByBullet(HitByBulletEvent event) {
    onEvent(event);
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    onEvent(event);
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    onEvent(event);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent event) {
    onEvent(event);
  }

  @Override
  public void onRoundEnded(RoundEndedEvent event) {
    onEvent(event);
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent event) {
    onEvent(event);
  }

  @Override
  public void onStatus(StatusEvent e) {
    onEvent(e);
  }

  @Override
  public void onWin(WinEvent event) {
    onEvent(event);
  }

}
