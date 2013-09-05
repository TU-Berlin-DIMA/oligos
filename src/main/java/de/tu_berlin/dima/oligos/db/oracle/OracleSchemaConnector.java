package de.tu_berlin.dima.oligos.db.oracle;

import java.sql.SQLException;
import java.util.Set;

import org.javatuples.Quartet;

import de.tu_berlin.dima.oligos.db.SchemaConnector;

public class OracleSchemaConnector implements SchemaConnector {

	@Override
	public Set<Quartet<String, String, String, String>> getReferences(
			String schema) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
