package de.tu_berlin.dima.oligos.db.oracle;

import java.sql.SQLException;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;

public class OracleTableConnector implements TableConnector {

	public OracleTableConnector(JdbcConnector jdbcConnector) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getCardinality(String schema, String table) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

}
