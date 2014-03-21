package mb.robocode.vector;

public class Target {
  public final String name;
  public final Vector pos;
  public final Vector vel;
  public final long time;
  
  public static final int STALE_LIMIT = 5;

  public Target(final String name, final Vector pos, final Vector vel,
      final long time) {
    this.name = name;
    this.pos = pos;
    this.vel = vel;
    this.time = time;
  }

  public boolean isStale(final long curTime) {
    return curTime - time > STALE_LIMIT;
  }
}