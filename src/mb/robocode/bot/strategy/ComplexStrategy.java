package mb.robocode.bot.strategy;

import java.util.ArrayList;
import java.util.Collection;

import mb.robocode.api.Action;

public class ComplexStrategy implements Strategy {
  
  private final Collection<Strategy> dependencies;
  
  public ComplexStrategy(final Collection<Strategy> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public Collection<Action> getActions() {
    final Collection<Action> retVal = new ArrayList<Action>();
    for (final Strategy dependency : dependencies) {
      retVal.addAll(dependency.getActions());
    }

    return retVal;
  }

}
