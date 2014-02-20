package mb.robocode.util;

public final class Vector implements Comparable<Vector> {
  
  public final double x;
  public final double y;

  public Vector(final double x, final double y) {
    this.x = x;
    this.y = y;
  }
  
  public Vector add(final Vector v) {
    return new Vector(x + v.x, y + v.y);
  }
  
  public Vector minus(final Vector v) {
    return new Vector(x - v.x, y - v.y);
  }
  
  public double abs() {
    return Math.sqrt(x * x + y * y);
  }

  public static Vector polarToComponent(final double heading, final double magnitude) {
    return new Vector(magnitude * Math.sin(heading), magnitude * Math.cos(heading));
  }

  @Override
  public int compareTo(final Vector v) {
    return (int) Math.signum(abs() - v.abs());
  }
}
