package mb.robocode.bot.strategy;

import java.util.Collection;

import mb.robocode.api.Action;

public class SimpleStrategy implements Strategy {
  
  private final Collection<Action> actions;
  
  public SimpleStrategy(final Collection<Action> actions) {
    this.actions = actions;
  }

  @Override
  public Collection<Action> getActions() {
    return actions;
  }

}
