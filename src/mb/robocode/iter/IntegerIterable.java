package mb.robocode.iter;

import java.util.Iterator;

public class IntegerIterable implements Iterable<Integer> {

  private final Integer start;
  private final Integer end;
  private final Integer step;

  /**
   * @param start
   *          (inclusive)
   * @param end
   *          (exclusive)
   */
  public IntegerIterable(final Integer start, final Integer end,
      final Integer step) {
    this.start = start;
    this.end = end;
    if (step == 0) {
      throw new IllegalArgumentException("Step size cannot be 0.");
    }
    this.step = step;
  }
  
  /**
   * @param start
   *          (inclusive)
   * @param end
   *          (exclusive)
   */
  public IntegerIterable(final Integer start, final Integer end) {
    this(start, end, 1);
  }
  
  @Override
  public IntegerIterator iterator() {
    if (step > 0) {
      return new IntegerIterator() {
        @Override
        public boolean hasNext() {
          return cur < end;
        }
      };
    } else if (step < 0) {
      return new IntegerIterator() {
        @Override
        public boolean hasNext() {
          return cur > end;
        }
      };
    } else {
      throw new IllegalStateException("Cannot have step size of 0.");
    }
  }

  private abstract class IntegerIterator implements Iterator<Integer> {
    
    protected Integer cur = start;

    @Override
    public Integer next() {
      final Integer retVal = cur;
      cur += step;
      
      return retVal;
    }

    @Override
    public void remove() {
    }

  }

}
