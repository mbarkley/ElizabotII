package mb.robot.api;

import java.util.Collection;
import java.util.List;

public interface Brain {
  
  public Collection<Action> getActions(final List<EventMap> events);

}
