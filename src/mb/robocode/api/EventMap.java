package mb.robocode.api;

import java.util.Collection;
import java.util.Map;

import robocode.Event;

public interface EventMap extends Map<Class<? extends Event>, Collection<Event>> {

  public <E extends Event> Collection<E> safeGet(Class<E> eventType);

}
