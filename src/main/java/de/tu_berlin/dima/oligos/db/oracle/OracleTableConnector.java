package de.tu_berlin.dima.oligos.db.oracle;

import java.math.BigDecimal;
import java.sql.SQLException;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;

public class OracleTableConnector implements TableConnector {

  private final static String QUERY =
          "SELECT table_name, num_rows FROM all_tables WHERE owner = ? AND table_name = ?";

  private final JdbcConnector connector;

	public OracleTableConnector(JdbcConnector connector) {
    this.connector = connector;
	}

	@Override
	public long getCardinality(String schema, String table) throws SQLException {
    return connector.<BigDecimal>scalarQuery(QUERY, "NUM_ROWS", schema, table).longValueExact();
	}

}
