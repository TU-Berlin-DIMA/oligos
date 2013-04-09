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

import java.sql.Time;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class TimeOperator implements Operator<Time> {

  @Override
  public int compare(Time o1, Time o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Time increment(Time value) {
    return Operators.increment(value);
  }

  @Override
  public Time decrement(Time value) {
    return Operators.decrement(value);
  }

  @Override
  public long range(Time val1, Time val2) {
    return val2.getTime() - val1.getTime() + 1;
  }

  @Override
  public Time min(Time val1, Time val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  @Override
  public Time max(Time val1, Time val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
