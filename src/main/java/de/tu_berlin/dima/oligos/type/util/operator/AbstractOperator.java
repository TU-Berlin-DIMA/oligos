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
package de.tu_berlin.dima.oligos.type.util.operator;

public abstract class AbstractOperator<T extends Comparable<T>>
implements Operator<T> {

  @Override
  public int compare(T o1, T o2) {
    return o1.compareTo(o2);
  }

  @Override
  public T min(T val1, T val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2; 
  }

  @Override
  public T max(T val1, T val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
