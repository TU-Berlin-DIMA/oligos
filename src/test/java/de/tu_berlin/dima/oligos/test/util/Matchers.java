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
package de.tu_berlin.dima.oligos.test.util;

import static org.hamcrest.Matchers.equalTo;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.Every;

import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.SchemaRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;

public final class Matchers {

  private Matchers() {}

  public static Matcher<SchemaRef> hasSchema(SchemaRef schema) {
    return hasSchema(equalTo(schema));
  }

  public static Matcher<SchemaRef> hasSchema(Matcher<? super SchemaRef> subMatcher) {
    return new FeatureMatcher<SchemaRef, SchemaRef>(subMatcher, "", "") {

      @Override
      protected SchemaRef featureValueOf(SchemaRef actual) {
        return actual.getSchema();
      }
    };
  }

  public static Matcher<TableRef> hasTable(TableRef table) {
    return hasTable(equalTo(table));
  }

  public static Matcher<TableRef> hasTable(Matcher<? super TableRef> subMatcher) {
    return new FeatureMatcher<TableRef, TableRef>(subMatcher, "", "") {

      @Override
      protected TableRef featureValueOf(TableRef actual) {
        return actual.getTable();
      }
    };
  }

  public static Matcher<ColumnRef> hasColumn(ColumnRef column) {
    return hasColumn(equalTo(column));
  }

  public static Matcher<ColumnRef> hasColumn(Matcher<? super ColumnRef> subMatcher) {
    return new FeatureMatcher<ColumnRef, ColumnRef>(subMatcher, "", "") {

      @Override
      protected ColumnRef featureValueOf(ColumnRef actual) {
        return actual.getColumn();
      }
    };
  }

  public static Every<TableRef> everyTable(Matcher<? super TableRef> itemMatcher) {
    return new Every<TableRef>(itemMatcher);
  }

  public static Every<ColumnRef> everyColumn(Matcher<? super ColumnRef> itemMatcher) {
    return new Every<ColumnRef>(itemMatcher);
  }

  public static Matcher<ForeignKey> hasParentSchema(final SchemaRef schema) {
    return hasParentSchema(equalTo(schema));
  }

  public static Matcher<ForeignKey> hasParentSchema(
      final Matcher<? super SchemaRef> subMatcher) {
    return new FeatureMatcher<ForeignKey, SchemaRef>(subMatcher, "", "") {
      @Override
      protected SchemaRef featureValueOf(ForeignKey actual) {
        return actual.getParent().getSchema();
      }
    };
  }

  public static Matcher<ForeignKey> hasParentTable(final TableRef table) {
    return hasParentTable(equalTo(table));
  }

  public static Matcher<ForeignKey> hasParentTable(
      final Matcher<? super TableRef> subMatcher) {
    return new FeatureMatcher<ForeignKey, TableRef>(subMatcher, "", "") {
      @Override
      protected TableRef featureValueOf(ForeignKey actual) {
        return actual.getParent();
      }
    };
  }

  public static Matcher<ForeignKey> hasChildSchema(final SchemaRef schema) {
    return hasChildSchema(equalTo(schema));
  }

  public static Matcher<ForeignKey> hasChildSchema(
      final Matcher<? super SchemaRef> subMatcher) {
    return new FeatureMatcher<ForeignKey, SchemaRef>(subMatcher, "", "") {
      @Override
      protected SchemaRef featureValueOf(ForeignKey actual) {
        return actual.getChild().getSchema();
      }
    };
  }

  public static Matcher<ForeignKey> hasChildTable(final TableRef table) {
    return hasChildTable(equalTo(table));
  }

  public static Matcher<ForeignKey> hasChildTable(
      final Matcher<? super TableRef> subMatcher) {
    return new FeatureMatcher<ForeignKey, TableRef>(subMatcher, "", "") {
      @Override
      protected TableRef featureValueOf(ForeignKey actual) {
        return actual.getChild();
      }
    };
  }

  public static <T> Every<T> everyItem(final Matcher<? super T> itemMatcher) {
    return new Every<T>(itemMatcher);
  }

}
