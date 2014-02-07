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

import java.util.ArrayList;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

import de.tu_berlin.dima.oligos.db.constraints.ForeignKey;
import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import de.tu_berlin.dima.oligos.db.reference.SchemaRef;
import de.tu_berlin.dima.oligos.db.reference.TableRef;

public final class TPCHModel extends ParameterSupplier {

  /*
   * Schema for TPCH
   */
  private final static SchemaRef SCHEMA = new SchemaRef("TESTUSER");
  
  /*
   * Tables for TPCH
   */
  private final static TableRef CUSTOMER = new TableRef(SCHEMA, "CUSTOMER");
  private final static TableRef LINEITEM = new TableRef(SCHEMA, "LINEITEM");
  private final static TableRef NATION = new TableRef(SCHEMA, "NATION");
  private final static TableRef ORDERS = new TableRef(SCHEMA, "ORDERS");
  private final static TableRef PART = new TableRef(SCHEMA, "PART");
  private final static TableRef PARTSUPP = new TableRef(SCHEMA, "PARTSUPP");
  private final static TableRef REGION = new TableRef(SCHEMA, "REGION");
  private final static TableRef SUPPLIER = new TableRef(SCHEMA, "SUPPLIER");
  
  /*
   * Customer columns
   */
  private final static ColumnRef C_ACCTBAL = new ColumnRef(CUSTOMER, "C_ACCTBAL");
  private final static ColumnRef C_ADDRESS = new ColumnRef(CUSTOMER, "C_ADDRESS");
  private final static ColumnRef C_COMMENT = new ColumnRef(CUSTOMER, "C_COMMENT");
  private final static ColumnRef C_CUSTKEY = new ColumnRef(CUSTOMER, "C_CUSTKEY");
  private final static ColumnRef C_MKTSEGMENT = new ColumnRef(CUSTOMER, "C_MKTSEGMENT");
  private final static ColumnRef C_NAME = new ColumnRef(CUSTOMER, "C_NAME");
  private final static ColumnRef C_NATIONKEY = new ColumnRef(CUSTOMER, "C_NATIONKEY");
  private final static ColumnRef C_PHONE = new ColumnRef(CUSTOMER, "C_PHONE");

  /*
   * Lineitem columns
   */
  private final static ColumnRef L_COMMENT = new ColumnRef(LINEITEM, "L_COMMENT");
  private final static ColumnRef L_COMMITDATE = new ColumnRef(LINEITEM, "L_COMMITDATE");
  private final static ColumnRef L_DISCOUNT = new ColumnRef(LINEITEM, "L_DISCOUNT");
  private final static ColumnRef L_EXTENDEDPRICE = new ColumnRef(LINEITEM, "L_EXTENDEDPRICE");
  private final static ColumnRef L_LINENUMBER = new ColumnRef(LINEITEM, "L_LINENUMBER");
  private final static ColumnRef L_LINESTATUS = new ColumnRef(LINEITEM, "L_LINESTATUS");
  private final static ColumnRef L_ORDERKEY = new ColumnRef(LINEITEM, "L_ORDERKEY");
  private final static ColumnRef L_PARTKEY = new ColumnRef(LINEITEM, "L_PARTKEY");
  private final static ColumnRef L_QUANTITY = new ColumnRef(LINEITEM, "L_QUANTITY");
  private final static ColumnRef L_RECEIPTDATE = new ColumnRef(LINEITEM, "L_RECEIPTDATE");
  private final static ColumnRef L_RETURNFLAG = new ColumnRef(LINEITEM, "L_RETURNFLAG");
  private final static ColumnRef L_SHIPDATE = new ColumnRef(LINEITEM, "L_SHIPDATE");
  private final static ColumnRef L_SHIPINSTRUCT = new ColumnRef(LINEITEM, "L_SHIPINSTRUCT");
  private final static ColumnRef L_SHIPMODE = new ColumnRef(LINEITEM, "L_SHIPMODE");
  private final static ColumnRef L_SUPPKEY = new ColumnRef(LINEITEM, "L_SUPPKEY");
  private final static ColumnRef L_TAX = new ColumnRef(LINEITEM, "L_TAX");

  /*
   * Nation columns
   */
  private final static ColumnRef N_COMMENT = new ColumnRef(NATION, "N_COMMENT");
  private final static ColumnRef N_NAME = new ColumnRef(NATION, "N_NAME");
  private final static ColumnRef N_NATIONKEY = new ColumnRef(NATION, "N_NATIONKEY");
  private final static ColumnRef N_REGIONKEY = new ColumnRef(NATION, "N_REGIONKEY");

  /*
   * Orders columns
   */
  private final static ColumnRef O_CLERK = new ColumnRef(ORDERS, "O_CLERK");
  private final static ColumnRef O_COMMENT = new ColumnRef(ORDERS, "O_COMMENT");
  private final static ColumnRef O_CUSTKEY = new ColumnRef(ORDERS, "O_CUSTKEY");
  private final static ColumnRef O_ORDERDATE = new ColumnRef(ORDERS, "O_ORDERDATE");
  private final static ColumnRef O_ORDERKEY = new ColumnRef(ORDERS, "O_ORDERKEY");
  private final static ColumnRef O_ORDERPRIORITY = new ColumnRef(ORDERS, "O_ORDERPRIORITY");
  private final static ColumnRef O_ORDERSTATUS = new ColumnRef(ORDERS, "O_ORDERSTATUS");
  private final static ColumnRef O_SHIPPRIORITY = new ColumnRef(ORDERS, "O_SHIPPRIORITY");
  private final static ColumnRef O_TOTALPRICE = new ColumnRef(ORDERS, "O_TOTALPRICE");

  /*
   * Partsupp columns
   */
  private final static ColumnRef PS_AVAILQTY = new ColumnRef(PARTSUPP, "PS_AVAILQTY");
  private final static ColumnRef PS_COMMENT = new ColumnRef(PARTSUPP, "PS_COMMENT");
  private final static ColumnRef PS_PARTKEY = new ColumnRef(PARTSUPP, "PS_PARTKEY");
  private final static ColumnRef PS_SUPPKEY = new ColumnRef(PARTSUPP, "PS_SUPPKEY");
  private final static ColumnRef PS_SUPPLYCOST = new ColumnRef(PARTSUPP, "PS_SUPPLYCOST");

  /*
   * Part columns
   */
  private final static ColumnRef P_BRAND = new ColumnRef(PART, "P_BRAND");
  private final static ColumnRef P_COMMENT = new ColumnRef(PART, "P_COMMENT");
  private final static ColumnRef P_CONTAINER = new ColumnRef(PART, "P_CONTAINER");
  private final static ColumnRef P_MFGR = new ColumnRef(PART, "P_MFGR");
  private final static ColumnRef P_NAME = new ColumnRef(PART, "P_NAME");
  private final static ColumnRef P_PARTKEY = new ColumnRef(PART, "P_PARTKEY");
  private final static ColumnRef P_RETAILPRICE = new ColumnRef(PART, "P_RETAILPRICE");
  private final static ColumnRef P_SIZE = new ColumnRef(PART, "P_SIZE");
  private final static ColumnRef P_TYPE = new ColumnRef(PART, "P_TYPE");

  /*
   * Region columns
   */
  private final static ColumnRef R_COMMENT = new ColumnRef(REGION, "R_COMMENT");
  private final static ColumnRef R_NAME = new ColumnRef(REGION, "R_NAME");
  private final static ColumnRef R_REGIONKEY = new ColumnRef(REGION, "R_REGIONKEY");

  /*
   * Supplier columns
   */
  private final static ColumnRef S_ACCTBAL = new ColumnRef(SUPPLIER, "S_ACCTBAL");
  private final static ColumnRef S_ADDRESS = new ColumnRef(SUPPLIER, "S_ADDRESS");
  private final static ColumnRef S_COMMENT = new ColumnRef(SUPPLIER, "S_COMMENT");
  private final static ColumnRef S_NAME = new ColumnRef(SUPPLIER, "S_NAME");
  private final static ColumnRef S_NATIONKEY = new ColumnRef(SUPPLIER, "S_NATIONKEY");
  private final static ColumnRef S_PHONE = new ColumnRef(SUPPLIER, "S_PHONE");
  private final static ColumnRef S_SUPPKEY = new ColumnRef(SUPPLIER, "S_SUPPKEY");

  private final static ForeignKey CUSTOMER_FK = ForeignKey.builder()
      .setName("CUSTOMER_FK")
      .addColumns(C_NATIONKEY, N_NATIONKEY)
      .build();
  private final static ForeignKey LINEITEM_FK1 = ForeignKey.builder()
      .setName("LINEITEM_FK1")
      .addColumns(L_PARTKEY, PS_PARTKEY)
      .addColumns(L_SUPPKEY, PS_SUPPKEY)
      .build();
  private final static ForeignKey LINEITEM_FK2 = ForeignKey.builder()
      .setName("LINEITEM_FK2")
      .addColumns(L_ORDERKEY, O_ORDERKEY)
      .build();
  private final static ForeignKey NATION_FK = ForeignKey.builder()
      .setName("NATION_FK")
      .addColumns(N_REGIONKEY, R_REGIONKEY)
      .build();
  private final static ForeignKey ORDERS_FK = ForeignKey.builder()
      .setName("ORDERS_FK")
      .addColumns(O_CUSTKEY, C_CUSTKEY)
      .build();
  private final static ForeignKey PARTSUPP_FK1 = ForeignKey.builder()
      .setName("PARTSUPP_FK1")
      .addColumns(PS_PARTKEY, P_PARTKEY)
      .build();
  private final static ForeignKey PARTSUPP_FK2 = ForeignKey.builder()
      .setName("PARTSUPP_FK2")
      .addColumns(PS_SUPPKEY, S_SUPPKEY)
      .build();
  private final static ForeignKey SUPPLIER_FK = ForeignKey.builder()
      .setName("SUPPLIER_FK")
      .addColumns(S_NATIONKEY, N_NATIONKEY)
      .build();

  /**
   * All tables unique to the TPCH schema
   */
  private final static TableRef[] TABLES = {
    CUSTOMER, LINEITEM, NATION, ORDERS, PARTSUPP, PART, REGION, SUPPLIER
  };

  /**
   * All columns unique to the TPCH schema
   */
  private final static ColumnRef[] COLUMNS = {
    // customer
    C_ACCTBAL, C_ADDRESS, C_COMMENT, C_CUSTKEY, C_MKTSEGMENT, C_NAME,
    C_NATIONKEY, C_PHONE,
    // lineitem
    L_COMMENT, L_COMMITDATE, L_DISCOUNT, L_EXTENDEDPRICE, L_LINENUMBER,
    L_LINESTATUS, L_ORDERKEY, L_PARTKEY, L_QUANTITY, L_RECEIPTDATE, L_RETURNFLAG,
    L_SHIPDATE, L_SHIPINSTRUCT, L_SHIPMODE, L_SUPPKEY, L_TAX,
    // nation
    N_COMMENT, N_NAME, N_NATIONKEY, N_REGIONKEY,
    // orders
    O_CLERK, O_COMMENT, O_CUSTKEY, O_ORDERDATE, O_ORDERKEY, O_ORDERPRIORITY,
    O_ORDERSTATUS, O_SHIPPRIORITY, O_TOTALPRICE,
    // partsupp
    PS_AVAILQTY, PS_COMMENT, PS_PARTKEY, PS_SUPPKEY, PS_SUPPLYCOST,
    // part
    P_BRAND, P_COMMENT, P_CONTAINER, P_MFGR, P_NAME, P_PARTKEY,
    P_RETAILPRICE, P_SIZE, P_TYPE,
    // region
    R_COMMENT, R_NAME, R_REGIONKEY,
    // supplier
    S_ACCTBAL, S_ADDRESS, S_COMMENT, S_NAME, S_NATIONKEY, S_PHONE, S_SUPPKEY
  };

  private final static ForeignKey[] FOREIGN_KEYS = {
    CUSTOMER_FK, LINEITEM_FK1, LINEITEM_FK2, NATION_FK, ORDERS_FK, PARTSUPP_FK1,
    PARTSUPP_FK2, SUPPLIER_FK
  };

  //private final static Collection<ColumnRef> ALL_COLUMNS = Arrays.asList(COLUMNS);

  public TPCHModel() {}

  public static SchemaRef[] getSchemas() {
    return new SchemaRef[]{SCHEMA};
  }

  public static TableRef[] getTables() {
    return TABLES;
  }

  public static TableRef[] getTables(SchemaRef schema) {
    return TABLES;
  }

  public static ColumnRef[] getColumns() {
    return COLUMNS;
  }

  public static ColumnRef[] getColumns(SchemaRef schema) {
    return COLUMNS;
  }

  public static ColumnRef[] getColumns(TableRef table) {
    ArrayList<ColumnRef> cols = new ArrayList<ColumnRef>();
    for (ColumnRef col : COLUMNS) {
      if (col.getTable().equals(table)) {
        cols.add(col);
      }
    }
    return cols.toArray(new ColumnRef[cols.size()]);
  }

  public static ForeignKey[] getForeignKeys() {
    return FOREIGN_KEYS;
  }

  public static ForeignKey[] getForeignKeys(final SchemaRef schema) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getParent().getSchema().equals(schema) ||
          fk.getChild().getSchema().equals(schema)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getForeignKeys(final TableRef table) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getParent().equals(table) ||
          fk.getChild().equals(table)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getCrossReferences(
      final SchemaRef firstSchema, final SchemaRef secondSchema) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      SchemaRef parentSchema = fk.getParent().getSchema();
      SchemaRef childSchema = fk.getChild().getSchema();
      if ((parentSchema.equals(firstSchema) && childSchema.equals(secondSchema))
          ||
          (childSchema.equals(firstSchema) && parentSchema.equals(secondSchema))) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getCrossReferences(
      final TableRef firstTable, final TableRef secondTable) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      SchemaRef parentTable = fk.getParent();
      SchemaRef childTable = fk.getChild();
      if ((parentTable.equals(firstTable) && childTable.equals(secondTable))
          ||
          (childTable.equals(firstTable) && parentTable.equals(secondTable))) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getExportedKeys(final SchemaRef schema) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getParent().getSchema().equals(schema) &&
          !fk.getChild().getSchema().equals(schema)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getExportedKeys(final TableRef table) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getParent().equals(table) &&
          !fk.getChild().equals(table)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getImportedKeys(final SchemaRef schema) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getChild().getSchema().equals(schema) &&
          !fk.getParent().getSchema().equals(schema)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  public static ForeignKey[] getImportedKeys(final TableRef table) {
    ArrayList<ForeignKey> fks = new ArrayList<ForeignKey>();
    for (ForeignKey fk : FOREIGN_KEYS) {
      if (fk.getChild().equals(table) &&
          !fk.getParent().equals(table)) {
        fks.add(fk);
      }
    }
    return fks.toArray(new ForeignKey[fks.size()]);
  }

  @Override
  public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
    List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
    Class<?> type = sig.getType();
    if (type.equals(SchemaRef.class)) {
      for (SchemaRef schema : getSchemas()) {
        assignments.add(PotentialAssignment.forValue(schema.toString(), schema));
      }
    } else if (type.equals(TableRef.class)) {
      for (TableRef table : getTables()) {
        assignments.add(PotentialAssignment.forValue(table.toString(), table));
      }
    } else if (type.equals(ColumnRef.class)) {
      for (ColumnRef column : getColumns()) {
        assignments.add(PotentialAssignment.forValue(column.toString(), column));
      }
    } else {
      throw new RuntimeException(
          "Unable to supply values of type " + type.getSimpleName());
    }
    return assignments;
  }

  /*public Collection<SchemaRef> getSchemas() {
    Set<SchemaRef> schemas = new HashSet<SchemaRef>();
    for (ColumnRef column : ALL_COLUMNS) {
      SchemaRef schema = column.getSchema();
      schemas.add(schema);
    }
    return schemas;
  }

  public Collection<TableRef> getTables() {
    Set<TableRef> tables = new HashSet<TableRef>();
    for (ColumnRef column : ALL_COLUMNS) {
      TableRef table = column.getTable();
      tables.add(table);
    }
    return tables;
  }

  public Collection<TableRef> getTables(final SchemaRef schema) {
    Set<TableRef> tables = new HashSet<TableRef>();
    for (ColumnRef column : ALL_COLUMNS) {
      if (column.getSchema().equals(schema)) {
        tables.add(column.getTable());
      }
    }
    return tables;
  }

  public Collection<ColumnRef> getColumns() {
    return new HashSet<ColumnRef>(ALL_COLUMNS);
  }

  public Collection<ColumnRef> getColumns(final SchemaRef schema) {
    Set<ColumnRef> columns = new HashSet<ColumnRef>();
    for (ColumnRef column : ALL_COLUMNS) {
      if (column.getSchema().equals(schema)) {
        columns.add(column);
      }
    }
    return columns;
  }

  public Collection<ColumnRef> getColumns(final TableRef table) {
    Set<ColumnRef> columns = new HashSet<ColumnRef>();
    for (ColumnRef column : ALL_COLUMNS) {
      if (column.getTable().equals(table)) {
        columns.add(column);
      }
    }
    return columns;
  }*/

}
