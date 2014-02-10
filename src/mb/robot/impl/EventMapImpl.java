package mb.robot.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import robocode.Event;
import mb.robot.api.EventMap;

public class EventMapImpl implements EventMap {

  private final Map<Class<? extends Event>, Collection<Event>> backingMap;

  public EventMapImpl(
      final Map<Class<? extends Event>, Collection<Event>> backingMap) {
    this.backingMap = backingMap;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <E extends Event> Collection<E> safeGet(Class<E> eventType) {
    if (!backingMap.containsKey(eventType)) {
      backingMap.put(eventType, (Collection<Event>) new ArrayList<E>());
    }
    
    return (Collection<E>) backingMap.get(eventType);
  }

  @Override
  public int size() {
    return backingMap.size();
  }

  @Override
  public boolean isEmpty() {
    return backingMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return backingMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return backingMap.containsValue(value);
  }

  @Override
  public Collection<Event> get(Object key) {
    return backingMap.get(key);
  }

  @Override
  public Collection<Event> put(Class<? extends Event> key,
      Collection<Event> value) {
    return backingMap.put(key, value);
  }

  @Override
  public Collection<Event> remove(Object key) {
    return backingMap.remove(key);
  }

  @Override
  public void clear() {
    backingMap.clear();
  }

  @Override
  public Set<Class<? extends Event>> keySet() {
    return backingMap.keySet();
  }

  @Override
  public Collection<Collection<Event>> values() {
    return backingMap.values();
  }

  @Override
  public void putAll(
      Map<? extends Class<? extends Event>, ? extends Collection<Event>> m) {
    for (final java.util.Map.Entry<? extends Class<? extends Event>, ? extends Collection<Event>> entry : m
        .entrySet()) {
      if (backingMap.containsKey(entry.getKey())) {
        backingMap.get(entry.getKey()).addAll(entry.getValue());
      } else {
        backingMap.put(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public Set<java.util.Map.Entry<Class<? extends Event>, Collection<Event>>> entrySet() {
    return backingMap.entrySet();
  }

}
