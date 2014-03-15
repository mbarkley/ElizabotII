package mb.robocode.util;

public final class Vector implements Comparable<Vector> {
  
  public final double x;
  public final double y;

  public Vector(final double x, final double y) {
    this.x = x;
    this.y = y;
  }
  
  public double dot(final Vector v) {
    return x * v.x + y * v.y;
  }
  
  public double angle(final Vector v) {
    final double cosTheta = this.dot(v) / (v.abs() * this.abs());
    // protect against rounding errors
    return Math.acos(Math.min(Math.max(cosTheta, -1.0), 1.0));
  }
  
  public Vector add(final Vector v) {
    return new Vector(x + v.x, y + v.y);
  }
  
  public Vector minus(final Vector v) {
    return new Vector(x - v.x, y - v.y);
  }
  
  public Vector rotate(final double radians) {
    return polarToComponent(heading() + radians, abs());
  }
  
  public double heading() {
    final double raw = angle(new Vector(0.0, 1.0));
    
    return (x >= 0.0) ? raw : 2.0 * Math.PI - raw;
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
  
  @Override
  public String toString() {
    return String.format("(%.2f, %.2f)", x, y);
  }

  public boolean isBoundBy(final Vector v) {
    return v.x >= x && v.y >= y;
  }

  public Vector scale(final double scalar) {
    return new Vector(x * scalar, y * scalar);
  }
  
  public Vector normalize() {
    return new Vector(x / abs(), y / abs());
  }
}
