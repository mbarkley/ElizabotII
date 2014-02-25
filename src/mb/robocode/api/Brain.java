package mb.robocode.api;

import java.util.Collection;
import java.util.List;

import robocode.AdvancedRobot;

public interface Brain {
  
  public Collection<Action> getActions(final List<EventMap> events);
  
  public void init(final AdvancedRobot robot);

}
