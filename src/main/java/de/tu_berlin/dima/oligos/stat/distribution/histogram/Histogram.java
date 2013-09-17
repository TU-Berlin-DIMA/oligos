/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.stat.distribution.histogram;

import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;

import de.tu_berlin.dima.oligos.stat.distribution.Distribution;

/**
 * For all elements <code>e</code> per bucket <code>b</code><br/ >
 * <code>l_b &lt;= e &lt;= u_b </code><br />
 * holds.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * 
 * @param <T>
 */
public interface Histogram<T> extends Distribution<T>, Iterable<Bucket<T>> {

  public void add(T lowerBounds, T upperBound, long frequency);

  public int getNumberOfBuckets();

  public int getBucketOf(T value);

  public long getTotalNumberOfValues();
  
  //public long getFrequencyAt(int bucket);

  public Histogram<T> getExactValues();

  public Histogram<T> getNonExactValues();

  public SortedSet<T> getLowerBounds() throws SQLException;

  public SortedSet<T> getUpperBounds();

  public List<Long> getFrequencies();

  public boolean isEmpty();

}
