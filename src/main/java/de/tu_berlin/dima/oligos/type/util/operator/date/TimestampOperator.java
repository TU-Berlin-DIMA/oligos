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
package de.tu_berlin.dima.oligos.type.util.operator.date;

import java.sql.Timestamp;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class TimestampOperator implements Operator<Timestamp> {

  @Override
  public int compare(Timestamp o1, Timestamp o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Timestamp increment(Timestamp value) {
    return Operators.increment(value);
  }

  @Override
  public Timestamp decrement(Timestamp value) {
    return Operators.decrement(value);
  }

  @Override
  public long range(Timestamp val1, Timestamp val2) {
    // TODO neglect nano seconds
    return Math.abs(val1.getTime() - val2.getTime());
  }

  @Override
  public Timestamp min(Timestamp val1, Timestamp val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  @Override
  public Timestamp max(Timestamp val1, Timestamp val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
