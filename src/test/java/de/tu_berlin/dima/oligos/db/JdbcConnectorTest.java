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
package de.tu_berlin.dima.oligos.db;

import static de.tu_berlin.dima.oligos.test.util.Matchers.everyColumn;
import static de.tu_berlin.dima.oligos.test.util.Matchers.everyTable;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasChildSchema;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasChildTable;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasParentSchema;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasParentTable;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasSchema;
import static de.tu_berlin.dima.oligos.test.util.Matchers.hasTable;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.SchemaRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;
//bug in hamcrest everyItem Matcher
//https://github.com/hamcrest/JavaHamcrest/issues/40

@RunWith(Theories.class)
public class JdbcConnectorTest {

  @ClassRule
  public static TPCHDerby tpchDB = new TPCHDerby(false);

  /**
   * Current database connection
   */
  private JdbcConnector jdbcConnector;

  @Before
  public void setUp() throws SQLException {
    jdbcConnector = new JdbcConnector(tpchDB.getConnection());
  }

  @After 
  public void tearDown() throws SQLException {
    jdbcConnector = null;
  }

  /**
   * Tests {@link JdbcConnector#getSchemas()} and checks whether the retrieved
   * schemas contain at least the schemas unique to the database instance.
   * @throws SQLException
   */
  @Test
  public void testGetSchemas() throws SQLException {
    Collection<SchemaRef> actual = jdbcConnector.getSchemas();
    SchemaRef[] expected = TPCHModel.getSchemas();
    assertThat(actual, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(actual, hasItems(expected));
  }

  /**
   * Tests {@link JdbcConnector#getTables()} and checks if the retrieved
   * tables contain at least the tables unique to the database instance.
   * @throws SQLException
   */
  @Test
  public void testGetTables() throws SQLException {
    Collection<TableRef> actual = jdbcConnector.getTables();
    TableRef[] expected = TPCHModel.getTables();
    assertThat(actual, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(actual, hasItems(expected));
  }

  /**
   * Tests {@link JdbcConnector#getTables(SchemaRef)} and checks if the retrieved
   * tables are within the schema and contain at least the tables unique to the
   * database instance.
   * @param schema the schema part to filter the columns
   * @throws SQLException
   */
  @Theory
  public void testGetTablesForSchema(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef schema) throws SQLException {
    Collection<TableRef> tables = jdbcConnector.getTables(schema);
    TableRef[] expected = TPCHModel.getTables(schema);
    assertThat(tables, everyTable(hasSchema(schema)));
    assertThat(tables, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(tables, hasItems(expected));
  }

  /**
   * Tests {@link JdbcConnector#getColumns()} and checks if the retrieved columns
   * contain at least the columns unique to the database instance.
   * @throws SQLException
   */
  @Test
  public void testGetColumns() throws SQLException {
    Collection<ColumnRef> actual = jdbcConnector.getColumns();
    ColumnRef[] expected = TPCHModel.getColumns();
    assertThat(actual, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(actual, hasItems(expected));
  }

  /**
   * Tests {@link JdbcConnector#getColumns(SchemaRef)} and checks if the
   * retrieved columns are within the schema and contain at least the columns
   * unique to the database instance.
   * @param schema the schema part to filter the columns
   * @throws SQLException
   */
  @Theory
  public void testGetColumnsForSchema(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef schema) throws SQLException {
    Collection<ColumnRef> actual = jdbcConnector.getColumns(schema);
    ColumnRef[] expected = TPCHModel.getColumns(schema);
    assertThat(actual, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(actual, everyColumn(hasSchema(schema)));
    assertThat(actual, hasItems(expected));
  }

  /**
   * Tests {@link JdbcConnector#getColumns(TableRef)} and checks if the
   * retrieved columns are within the table and contain at least the columns
   * unique to the database instance.
   * @param table
   * @throws SQLException
   */
  @Theory
  public void testGetColumnsForTable(
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef table) throws SQLException {
    SchemaRef schema = table.getSchema();
    Collection<ColumnRef> actual = jdbcConnector.getColumns(table);
    ColumnRef[] expected = TPCHModel.getColumns(table);
    assertThat(actual, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(actual, everyColumn(hasSchema(schema)));
    assertThat(actual, everyColumn(hasTable(table)));
    assertThat(actual, hasItems(expected));
  }

  /**
   * TODO
   * @throws SQLException
   */
  @Ignore
  @Test
  public void testGetForeignKeys() throws SQLException {
    assertThat(null, not(anything()));
  }

  /**
   * Tests {@link JdbcConnector#getForeignKeys(SchemaRef)} and checks if the
   * retrieved foreign keys are referencing or referenced by the schema.
   * @param schema The schema to get the foreign keys for
   * @throws SQLException
   */
  @Theory
  public void testGetForeignKeysForSchema(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef schema) throws SQLException {
    Set<ForeignKey> foreignKeys = jdbcConnector.getForeignKeys(schema);
    assertThat(foreignKeys, 
        everyItem(anyOf(
            hasParentSchema(schema),
            hasChildSchema(schema))));
  }

  /**
   * Tests {@link JdbcConnector#getForeignKeys(TableRef)} and checks if the
   * retrieved foreign keys are referencing or referenced by the table.
   * @param table The table part to filter the foreign keys
   * @throws SQLException
   */
  @Theory
  public void testGetForeignKeysForTable(
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef table) throws SQLException {
    Set<ForeignKey> foreignKeys = jdbcConnector.getForeignKeys(table);
    assertThat(foreignKeys,
        everyItem(anyOf(
            hasParentTable(table),
            hasChildTable(table))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(foreignKeys, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(foreignKeys, hasItems(expected));
  }

  /**
   * TODO
   * @throws SQLException
   */
  @Test
  public void testGetCrossReferences() throws SQLException {
    Set<ForeignKey> crossReferences = jdbcConnector.getCrossReferences();
    // TODO get all foreign keys from the model
    ForeignKey[] expected = {};
    assertThat(crossReferences, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(crossReferences, hasItems(expected));
    assertThat(crossReferences, equalTo(jdbcConnector.getForeignKeys()));
  }

  /**
   * TODO
   * @param firstSchema
   * @param secondSchema
   * @throws SQLException
   */
  @Theory
  public void testGetCrossReferencesBetweenSchemas(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef firstSchema,
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef secondSchema) throws SQLException {
    // TODO
    Set<ForeignKey> crossReferences = 
        jdbcConnector.getCrossReferences(firstSchema, secondSchema);
    assertThat(crossReferences, 
        everyItem(
            anyOf(
                allOf(
                    hasChildSchema(firstSchema),
                    hasParentSchema(secondSchema)),
                allOf(
                    hasChildSchema(secondSchema),
                    hasParentSchema(firstSchema)))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(crossReferences, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(crossReferences, hasItems(expected));
  }

  /**
   * TODO
   * @param firstTable
   * @param secondTable
   * @throws SQLException
   */
  @Theory
  public void testGetCrossReferencesBetweenTables(
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef firstTable,
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef secondTable) throws SQLException {
    Set<ForeignKey> crossReferences =
        jdbcConnector.getCrossReferences(firstTable, secondTable);
    assertThat(crossReferences, 
        everyItem(
            anyOf(
                allOf(
                    hasChildTable(firstTable),
                    hasParentTable(secondTable)),
                allOf(
                    hasChildTable(secondTable),
                    hasParentTable(firstTable)))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(crossReferences, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(crossReferences, hasItems(expected));
  }

  /**
   * TODO
   * @param schema
   * @throws SQLException
   */
  @Theory
  public void testGetImportedKeysForSchema(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef schema) throws SQLException {
    Set<ForeignKey> importedKeys = jdbcConnector.getImportedKeys(schema);
    assertThat(importedKeys, everyItem(hasChildSchema(schema)));
    assertThat(importedKeys, everyItem(hasParentSchema(not(schema))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(importedKeys, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(importedKeys, hasItems(expected));
  }

  /**
   * TODO
   * @param table
   * @throws SQLException
   */
  @Theory
  public void testGetImportedKeysForTable(
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef table) throws SQLException {
    Set<ForeignKey> importedKeys = jdbcConnector.getImportedKeys(table);
    assertThat(importedKeys, everyItem(hasChildTable(equalTo(table))));
    assertThat(importedKeys, everyItem(hasParentTable(not(equalTo(table)))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(importedKeys, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(importedKeys, hasItems(expected));
  }

  /**
   * TODO
   * @param schema
   * @throws SQLException
   */
  @Theory
  public void testGetExportedKeysForSchema(
      @ParametersSuppliedBy(TPCHModel.class)
      final SchemaRef schema) throws SQLException {
    Set<ForeignKey> importedKeys = jdbcConnector.getExportedKeys(schema);
    assertThat(importedKeys, everyItem(hasParentSchema(schema)));
    assertThat(importedKeys, everyItem(hasChildSchema(not(schema))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(importedKeys, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(importedKeys, hasItems(expected));
  }

  /**
   * TODO
   * @param table
   * @throws SQLException
   */
  @Theory
  public void testGetExportedKeysForTables(
      @ParametersSuppliedBy(TPCHModel.class)
      final TableRef table) throws SQLException {
    Set<ForeignKey> importedKeys = jdbcConnector.getExportedKeys(table);
    assertThat(importedKeys, everyItem(hasParentTable(table)));
    assertThat(importedKeys, everyItem(hasChildTable(not(table))));
    // TODO fill with values from the model
    ForeignKey[] expected = {};
    assertThat(importedKeys, hasSize(greaterThanOrEqualTo(expected.length)));
    assertThat(importedKeys, hasItems(expected));
  }

}
