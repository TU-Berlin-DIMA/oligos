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
package de.tu_berlin.dima.oligos.db.handler.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.google.common.base.Predicate;

import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;
import static de.tu_berlin.dima.oligos.db.JdbcConstants.Keys.*;

/**
 * {@link org.apache.commons.dbutils.ResultSetHandler ResultSetHandler}
 * implementation that converts the given <code>ResultSet</code> into a
 * <code>Set</code> of <code>ForeignKey</code>s. This class is intended to be a
 * utility class when working with JDBC and its <code>DatabaseMetaData</code>
 * object.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class ForeignKeysHandler implements ResultSetHandler<Set<ForeignKey>> {
  
  private static final Logger LOGGER = Logger.getLogger(ForeignKeysHandler.class);

  private final Predicate<ResultSet> predicate;

  private boolean isPart;

  public ForeignKeysHandler() {
    this.predicate = new Predicate<ResultSet>() {
      @Override
      public boolean apply(@Nullable ResultSet input) {
        return true;
      }
    };
  }

  public ForeignKeysHandler(final Predicate<ResultSet> predicate) {
    this.predicate = predicate;
  }
  
  /**
   * Converts the given <code>ResultSet</code> into a <code>Set</code> of
   * <code>ForeignKey</code>s.
   * @param rs Untouched <code>ResultSet</code> with at least the following
   *   columns: 
   *   <ol>
   *     <li>PKTABLE_SCHEM String => primary key table schema being imported (may be null)</li>
   *     <li>PKTABLE_NAME String => primary key table name being imported</li>
   *     <li>PKCOLUMN_NAME String => primary key column name being imported</li>
   *     <li>FKTABLE_SCHEM String => foreign key table schema (may be null)</li>
   *     <li>FKTABLE_NAME String => foreign key table name</li>
   *     <li>FKCOLUMN_NAME String => foreign key column name</li>
   *     <li>KEY_SEQ short => sequence number within a foreign key</li>
   *   </ol>
   * @return A <code>Set</code> containing no, one, or more <code>ForeignKey</code>s
   */
  @Override
  public Set<ForeignKey> handle(ResultSet rs) throws SQLException {
    Set<ForeignKey> foreignKeys = new HashSet<ForeignKey>();
    ForeignKey last = null;
    ForeignKey current = null;
    while (rs.next() && predicate.apply(rs)) {
      current = handleRow(rs);
      if (isPart) {
        Collection<ColumnRef> childCols = current.getChildColumns();
        Collection<ColumnRef> parenCols = current.getParentColumns();
        last.addColumns(childCols, parenCols);
      } else {
        foreignKeys.add(current);
      }
      last = current;
    }
    return foreignKeys;
  }

  private ForeignKey handleRow(ResultSet rs) throws SQLException {
    Collection<String> parentColumns = new ArrayList<String>();
    Collection<String> childColumns = new ArrayList<String>();
    String parentSchema = rs.getString(PARENT_SCHEMA);
    if (parentSchema == null) parentSchema = "";
    String childSchema = rs.getString(CHILD_SCHEMA);
    if (childSchema == null) childSchema = "";
    TableRef parentTable = new TableRef(parentSchema, rs.getString(PARENT_TABLE));
    parentColumns.add(rs.getString(PARENT_COLUMN));
    TableRef childTable = new TableRef(childSchema, rs.getString(CHILD_TABLE));
    childColumns.add(rs.getString(CHILD_COLUMN));
    short seq = rs.getShort(SEQUENCE_NUMBER);
    isPart = seq > 1;
    ForeignKey foreignKey =
        new ForeignKey("", childTable, childColumns, parentTable, parentColumns);
    LOGGER.debug("Found foreign key: " + foreignKey);
    return foreignKey;
  }

}
