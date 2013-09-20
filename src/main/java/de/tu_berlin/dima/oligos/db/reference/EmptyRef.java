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
package de.tu_berlin.dima.oligos.db.reference;

/**
 * Empty reference that returns just null.
 * Depending on the usage it acts as a {@link SchemaRef}, {@link TableRef} or 
 * {@link ColumnRef}.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class EmptyRef extends ColumnRef {
  
  public EmptyRef()  {
    super("", "", "");
  }

  /**
   * @return null (always)
   */
  @Override
  public String getSchemaName() {
    return null;
  }

  /**
   * @return null (always)
   */
  @Override
  public String getTableName() {
    return null;
  }

  /**
   * @return null (always)
   */
  @Override
  public String getColumnName() {
    return null;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * @return an empty string
   */
  @Override
  public String toString() {
    return "";
  }

}
