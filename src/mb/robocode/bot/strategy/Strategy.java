package mb.robocode.bot.strategy;

import java.util.Collection;

import mb.robocode.api.Action;

public interface Strategy {
  
  public Collection<Action> getActions();

}
