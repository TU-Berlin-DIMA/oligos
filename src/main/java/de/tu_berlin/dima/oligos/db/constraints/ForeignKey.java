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
package de.tu_berlin.dima.oligos.db.constraints;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;

import static com.google.common.base.Preconditions.*;

/**
 * This class representing a foreign key in a relational database.
 * <br />
 * A foreign key is a reference between two tables. The referencing table
 * is called the child table and the referenced table is called the parent table.
 * The columns that are part of the foreign key have to be identical in number.
 * Consequently if a foreign key uses one, two, or more columns in the referencing
 * part (child) the referenced part has to contain the same amount of columns;
 * one, two or more respectively.
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class ForeignKey extends AbstractConstraint {

  /**
   * referenced table, i.e. primary key
   */
  private final TableRef parent;

  /**
   * referenced columns, i.e. primary key
   */
  private final Set<String> parentColumns;

  /**
   * referencing table, i.e. foreign key 
   */
  private final TableRef child;
  
  /**
   * referencing columns, i.e. foreign key
   */
  private final Set<String> childColumns;

  /**
   * Creates a new foreign key.
   * @param name The name of the foreign key
   * @param child The child/referencing  table 
   * @param childColumns The child/referencing columns
   * @param parent The parent/referenced table
   * @param parentColumns The parent/referenced columns
   */
  public ForeignKey(
      final String name,
      final TableRef child, final Collection<String> childColumns,
      final TableRef parent, final Collection<String> parentColumns) {
    super(name);
    checkArgument(childColumns.size() == parentColumns.size());
    this.child = child;
    this.childColumns = Sets.newHashSet(childColumns);
    this.parent = parent;
    this.parentColumns = Sets.newHashSet(parentColumns);
    assert(this.childColumns.size() == this.parentColumns.size());
  }

  /**
   * Obtains the parent table referenced by this foreign key.
   * @return The parent table
   */
  public TableRef getParent() {
    return parent;
  }

  /**
   * Obtains the parent columns referenced by this foreign key.
   * @return The parent columns
   */
  public Set<ColumnRef> getParentColumns() {
    return generateColumnRefs(parent, parentColumns);
  }

  /**
   * Obtains the child table referencing the parent table.
   * @return The child table
   */
  public TableRef getChild() {
    return child;
  }

  /**
   * Obtains the child columns referencing the parent columns.
   * @return The child columns
   */
  public Set<ColumnRef> getChildColumns() {
    return generateColumnRefs(child, childColumns);
  }

  /**
   * Adds the given columns to the child or parent columns. They must match the
   * child and parent table.
   * @param childColumn The column to add to the child columns.
   * @param parentColumn The column to add to the parent columns.
   */
  public void addColumn(ColumnRef childColumn, ColumnRef parentColumn) {
    Preconditions.checkArgument(childColumn.equals(child) && parentColumn.equals(parent));
    childColumns.add(childColumn.getColumnName());
    parentColumns.add(parentColumn.getColumnName());
  }

  /**
   * Adds the given columns to the child or parent columns. They must match the
   * child and parent table and must have the same number of columns.
   * @param childColumns The columns to add to the child columns.
   * @param parentColumns The columns to add to the parent columns.
   */
  public void addColumns(
      final Collection<ColumnRef> childColumns,
      final Collection<ColumnRef> parentColumns) {
    Preconditions.checkArgument(childColumns.size() == parentColumns.size(),
        "Number of columns have to match.");
    Iterator<ColumnRef> childIter = childColumns.iterator();
    Iterator<ColumnRef> parentIter = parentColumns.iterator();
    while (childIter.hasNext() && parentIter.hasNext()) {
      ColumnRef childCol = childIter.next();
      ColumnRef parenCol = parentIter.next();
      addColumn(childCol, parenCol);
    }
  }

  private Set<ColumnRef> generateColumnRefs(TableRef table, Set<String> columns) {
    Set<ColumnRef> colRefs = Sets.newHashSet();
    for (String col : columns) {
      colRefs.add(new ColumnRef(table, col));
    }
    return colRefs;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(parent, parentColumns, child, childColumns);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj instanceof ForeignKey){
      ForeignKey other = (ForeignKey) obj;
      return parent.equals(other.parent) &&
          parentColumns.equals(other.parentColumns) &&
          child.equals(other.child) &&
          childColumns.equals(other.childColumns);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder strBld = new StringBuilder();
    strBld.append(getName());
    strBld.append(": ");
    strBld.append(child.toString());
    strBld.append(childColumns.toString());
    strBld.append(" -> ");
    strBld.append(parent.toString());
    strBld.append(parentColumns.toString());
    return strBld.toString();
  }

}
