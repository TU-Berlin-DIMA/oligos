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
public class ColumnRef extends TableRef {

  private final String column;

  public ColumnRef(final String schema, final String table, final String column) {
    super(schema, table);
    Preconditions.checkArgument(column != null,
        "A column name must not be null. Use EmtpyRef instead.");
    this.column = column;
  }

  public ColumnRef(final TableRef tableRef, final String column) {
    this(tableRef.getSchemaName(), tableRef.getTableName(), column);
  }

  /**
   * Retrieves the column reference.
   * @return
   */
  public ColumnRef getColumn() {
    return new ColumnRef(getTable(), column);
  }

  /**
   * Retrieves the name of the column.
   * @return
   */
  public String getColumnName() {
    return column;
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      if (obj instanceof ColumnRef) {
        ColumnRef other = (ColumnRef) obj;
        return Objects.equal(column, other.column);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.getSchemaName(), super.getTableName(), column);
  }

  @Override
  public String toString() {
    return super.toString() + "."  + column;
  }
}
