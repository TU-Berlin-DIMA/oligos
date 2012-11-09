package de.tu_berlin.dima.oligos.db.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.javatuples.Quartet;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;

public class Db2SchemaConnector implements SchemaConnector {

  public final static String REFERENCES_QUERY =
      "SELECT reftbname as parent_table " +
      "     , tbname as child_table " +
      "     , pkcolnames " +
      "     , fkcolnames " +
      "FROM   SYSIBM.SYSRELS " +
      "WHERE  creator = ?";

  private final JdbcConnector connector;

  public Db2SchemaConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  @Override
  public Set<Quartet<String, String, String, String>> getReferences(final String schema)
      throws SQLException {
    Set<Quartet<String, String, String, String>> references = Sets.newHashSet();
    ResultSet result = connector.executeQuery(REFERENCES_QUERY, schema);
    while (result.next()) {
      String parentTable = result.getString("parent_table");
      String parentColumn = result.getString("pkcolnames").trim().split("\\s+")[0];
      String childTable = result.getString("child_table");
      String childColumn = result.getString("fkcolnames").trim().split("\\s+")[0];
      Quartet<String, String, String, String> ri =
          new Quartet<String, String, String, String>(
              parentTable, parentColumn, childTable, childColumn);
      references.add(ri);
    }
    return references;
  }

}
