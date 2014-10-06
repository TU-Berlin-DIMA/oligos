/*******************************************************************************
 * Copyright 2014 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
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
package de.tu_berlin.dima.oligos.util;

import static de.tu_berlin.dima.oligos.db.JdbcConstants.Keys.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import de.tu_berlin.dima.oligos.db.reference.SchemaRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;

public class Predicates {

  public static Predicate<ResultSet> hasChildSchema(final SchemaRef childSchema) {
    return new HasChildSchema(childSchema);
  }

  public static Predicate<ResultSet> hasChild(final TableRef child) {
    return new HasChild(child);
  }

  public static Predicate<ResultSet> hasParentSchema(final SchemaRef parentSchema) {
    return new HasParentSchema(parentSchema);
  }

  public static Predicate<ResultSet> hasParent(final TableRef parent) {
    return new HasParent(parent);
  }

  public static class HasParentSchema implements Predicate<ResultSet> {

    private final SchemaRef parentSchema;

    public HasParentSchema(final SchemaRef parentSchema) {
      this.parentSchema = parentSchema;
    }

    @Override
    public boolean apply(@Nullable ResultSet input) {
      try {
        String schemaName = input.getString(PARENT_SCHEMA);
        SchemaRef schema = new SchemaRef(schemaName);
        return parentSchema.equals(schema);
      } catch (SQLException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public static class HasParent implements Predicate<ResultSet> {

    private final TableRef parent;

    public HasParent(final TableRef parent) {
      this.parent = parent;
    }

    @Override
    public boolean apply(@Nullable ResultSet input) {
      try {
        String parentSchema = input.getString(PARENT_SCHEMA);
        String parentTable = input.getString(PARENT_TABLE);
        TableRef table = new TableRef(parentSchema, parentTable);
        return table.equals(parent);
      } catch (SQLException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public static class HasChildSchema implements Predicate<ResultSet> {

    private final SchemaRef childSchema;

    public HasChildSchema(final SchemaRef childSchema) {
      this.childSchema = childSchema;
    }

    @Override
    public boolean apply(@Nullable ResultSet input) {
      try {
        String schemaName = input.getString(CHILD_SCHEMA);
        SchemaRef schema = new SchemaRef(schemaName);
        return childSchema.equals(schema);
      } catch (SQLException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public static class HasChild implements Predicate<ResultSet> {

    private final TableRef child;

    public HasChild(final TableRef child) {
      this.child = child;
    }

    @Override
    public boolean apply(@Nullable ResultSet input) {
      try {
        String childSchema = input.getString(CHILD_SCHEMA);
        String childTable = input.getString(CHILD_TABLE);
        TableRef table = new TableRef(childSchema, childTable);
        return table.equals(child);
      } catch (SQLException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

}
