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

import java.sql.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class DateOperator implements Operator<Date> {

  @Override
  public int compare(Date o1, Date o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Date increment(Date value) {
    return Operators.increment(value);
  }

  @Override
  public Date decrement(Date value) {
    return Operators.decrement(value);
  }

  @Override
  public long range(Date val1, Date val2) {
    DateTime dt1 = new DateTime(val1);
    DateTime dt2 = new DateTime(val2);
    return Days.daysBetween(dt1, dt2).getDays();
  }

  @Override
  public Date min(Date val1, Date val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  @Override
  public Date max(Date val1, Date val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
