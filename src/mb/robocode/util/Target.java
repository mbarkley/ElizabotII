package mb.robocode.util;



public class Target {
  public final String name;
  public final Vector pos;
  public final Vector vel;
  public final long time;

  public Target(final String name, final Vector pos, final Vector vel,
      final long time) {
    this.name = name;
    this.pos = pos;
    this.vel = vel;
    this.time = time;
  }
}