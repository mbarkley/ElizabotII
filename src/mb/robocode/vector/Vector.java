package mb.robocode.vector;

public final class Vector implements Comparable<Vector> {
  
  private static final double EPSILON = 0.0000001;
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
  
  public Vector normalize() {
    final double abs = abs();
    return new Vector(x / abs, y / abs);
  }

  public static Vector polarToComponent(final double heading, final double magnitude) {
    return new Vector(magnitude * Math.sin(heading), magnitude * Math.cos(heading));
  }

  /**
   * This natural ordering is inconsistent with equals.
   */
  @Override
  public int compareTo(final Vector v) {
    return (int) Math.signum(abs() - v.abs());
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Vector && minus((Vector) obj).abs() < EPSILON;
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
}
