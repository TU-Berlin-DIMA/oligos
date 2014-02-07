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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Plain immutable object representing a column reference in a relational system.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class TableRef extends SchemaRef {

  private final String table;
  
  public TableRef(final String schema, final String table) {
    super(schema);
    Preconditions.checkArgument(table != null,
        "A table name must not be null. Use EmtpyRef instead.");
    this.table = table;
  }

  public TableRef(final SchemaRef schemaRef, final String table) {
    this(schemaRef.getSchemaName(), table);
  }

  /**
   * Retrieves the table reference.
   * @return
   */
  public TableRef getTable() {
    return new TableRef(getSchema(), table);
  }

  /**
   * Retrieves the name of the table.
   * @return
   */
  public String getTableName() {
    return table;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(super.getSchemaName(), table);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      if (obj instanceof TableRef) {
        TableRef other = (TableRef) obj;
        return Objects.equal(table, other.table);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return super.toString() + "." + getTableName();
  }
}
