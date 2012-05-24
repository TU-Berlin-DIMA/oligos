package de.tu_berlin.dima.oligos.histogram;

/**
 * 
 * 
 * Invariants over domain <i>D</i>:<br />
 * <ul>
 * <li><b>Number of bins</b> = <i>max(D) - min(D) / binSize</i></li>
 * <li>All values per bin are greater or equal AND strict smaller than the lower, resp. upper, bin bounds</li>
 * </ul>
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class EquiWidthHistogram {

  private final int minValue;
  private final int maxValue;
  private final int sizeOfBin;

  private final int[] lowerBounds;
  private final int[] upperBounds;
  private final int[] frequencies;
  
  private long totalValues = 0l;

  public EquiWidthHistogram(final int sizeOfBin, final int minValue,
      final int maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.sizeOfBin = sizeOfBin;
    int noBins = (int) Math.ceil((maxValue - minValue) / sizeOfBin);
    lowerBounds = new int[noBins];
    upperBounds = new int[noBins];
    frequencies = new int[noBins];

    // init bounds
    int lBound = minValue;
    int uBound = minValue + sizeOfBin;

    for (int i = 0; i < noBins; i++) {
      lowerBounds[i] = lBound;
      upperBounds[i] = uBound;

      lBound = uBound;
      uBound = lBound + sizeOfBin;
    }
  }
  
  public void addValue(int value) {
    int i = getBinIndex(value);
    frequencies[i] += 1;
    totalValues++;
  }

  public int getRange() {
    return maxValue - minValue;
  }

  public int getBinIndex(int value) {
    // TODO use constant formula instead
    int index = 0;
    while (value > upperBounds[index] && index < upperBounds.length) {
      index++;
    }
    return index;
  }
  
  public int getFrequency(int value) {
    int i = getBinIndex(value);
    return frequencies[i] / sizeOfBin;
  }

  public double getProbability(int value) {
    int i = getBinIndex(value);
    return frequencies[i] / totalValues;
  }
}
