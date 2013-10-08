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
package de.tu_berlin.dima.oligos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import oracle.sql.DATE;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.cli.CommandLineInterface;
import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.DbUtils;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.MetaConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2ColumnConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2MetaConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2SchemaConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2TableConnector;
import de.tu_berlin.dima.oligos.db.oracle.OracleColumnConnector;
import de.tu_berlin.dima.oligos.db.oracle.OracleMetaConnector;
import de.tu_berlin.dima.oligos.db.oracle.OracleSchemaConnector;
import de.tu_berlin.dima.oligos.db.oracle.OracleTableConnector;
import de.tu_berlin.dima.oligos.exception.TypeNotSupportedException;
import de.tu_berlin.dima.oligos.exception.UnsupportedTypeException;
import de.tu_berlin.dima.oligos.io.MyriadWriter;
import de.tu_berlin.dima.oligos.profiler.ColumnProfiler;
import de.tu_berlin.dima.oligos.profiler.PseudoColumnProfiler;
import de.tu_berlin.dima.oligos.profiler.SchemaProfiler;
import de.tu_berlin.dima.oligos.profiler.TableProfiler;
import de.tu_berlin.dima.oligos.stat.Schema;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.operator.AbstractOperator;
import de.tu_berlin.dima.oligos.type.util.operator.CharOperator;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.StringOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.DateOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.OracleDateOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.TimeOperator;
import de.tu_berlin.dima.oligos.type.util.operator.date.TimestampOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.BigDecimalOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.DoubleOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.FloatOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.IntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.LongOperator;
import de.tu_berlin.dima.oligos.type.util.operator.numerical.ShortOperator;
import de.tu_berlin.dima.oligos.type.util.parser.CharParser;
import de.tu_berlin.dima.oligos.type.util.parser.DateParser;
import de.tu_berlin.dima.oligos.type.util.parser.BigDecimalParser;
import de.tu_berlin.dima.oligos.type.util.parser.DoubleParser;
import de.tu_berlin.dima.oligos.type.util.parser.FloatParser;
import de.tu_berlin.dima.oligos.type.util.parser.IntegerParser;
import de.tu_berlin.dima.oligos.type.util.parser.LongParser;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;
import de.tu_berlin.dima.oligos.type.util.parser.ShortParser;
import de.tu_berlin.dima.oligos.type.util.parser.StringParser;
import de.tu_berlin.dima.oligos.type.util.parser.TimeParser;
import de.tu_berlin.dima.oligos.type.util.parser.TimestampParser;

public class Oligos {

  private static final Logger LOGGER = Logger.getLogger(Oligos.class);

  public static ColumnProfiler<?> getProfiler(final ColumnId columnId, final TypeInfo type
      , final JdbcConnector jdbcConnector, final MetaConnector metaConnector)
          throws SQLException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return getProfiler(schema, table, column, type, jdbcConnector, metaConnector);
  }
  
//TODO
  public static ColumnProfiler<?> getProfiler(final String schema, final  String table
      , final String column, final TypeInfo type, final JdbcConnector jdbcConnector
      , final MetaConnector metaConnector)
      throws SQLException {
  	
  	ColumnProfiler<?> profiler = null;
  	LOGGER.trace("type = " + type.toString());
    String typeName = type.getTypeName().toLowerCase();
    boolean isEnum = metaConnector.isEnumerated(schema, table, column);
    if (typeName.equals("smallint")) {
      Parser<Short> p = new ShortParser();
      Operator<Short> op = new ShortOperator();
      ColumnConnector<Short> connector = new Db2ColumnConnector<Short>(
          jdbcConnector, schema, table, column, p);      
      profiler = new ColumnProfiler<Short>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("integer")) {
      Parser<Integer> p = new IntegerParser();
      Operator<Integer> op = new IntegerOperator();
      ColumnConnector<Integer> connector = new Db2ColumnConnector<Integer>(
          jdbcConnector, schema, table, column, p);      
      profiler = new ColumnProfiler<Integer>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("bigint")) {
      Parser<Long> p = new LongParser();
      Operator<Long> op = new LongOperator();
      ColumnConnector<Long> connector = new Db2ColumnConnector<Long>(
          jdbcConnector, schema, table, column, p);      
      profiler = new ColumnProfiler<Long>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("decimal")) {
      Parser<BigDecimal> p = new BigDecimalParser();
      Operator<BigDecimal> op = new BigDecimalOperator();
      ColumnConnector<BigDecimal> connector = new Db2ColumnConnector<BigDecimal>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<BigDecimal>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("float")) {
      Parser<Float> p = new FloatParser();
      Operator<Float> op = new FloatOperator();
      ColumnConnector<Float> connector = new Db2ColumnConnector<Float>(
          jdbcConnector, schema, table, column, p);
      profiler = new ColumnProfiler<Float>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("double")) {
      Parser<Double> p = new DoubleParser();
      Operator<Double> op = new DoubleOperator();
      ColumnConnector<Double> connector = new Db2ColumnConnector<Double>(
          jdbcConnector, schema, table, column, p);
      profiler = new ColumnProfiler<Double>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("timestamp")) {
      Parser<Timestamp> p = new TimestampParser();
      Operator<Timestamp> op = new TimestampOperator();
      ColumnConnector<Timestamp> connector = new Db2ColumnConnector<Timestamp>(
          jdbcConnector, schema, table, column, p);
      profiler = new ColumnProfiler<Timestamp>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("time")) {
      Parser<Time> p = new TimeParser();
      Operator<Time> op = new TimeOperator();
      ColumnConnector<Time> connector = new Db2ColumnConnector<Time>(
          jdbcConnector, schema, table, column, p);
      profiler = new ColumnProfiler<Time>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if (typeName.equals("date")) {
      Parser<Date> p = new DateParser();
      Operator<Date> op = new DateOperator();
      ColumnConnector<Date> connector = new Db2ColumnConnector<Date>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<Date>(
          schema, table, column, type, isEnum, connector, op, p);
    } else if ((typeName.equals("char") || typeName.equals("varchar"))
        && (type.getLength() == 1)) {
      Parser<Character> p = new CharParser();
      Operator<Character> op = new CharOperator();
      ColumnConnector<Character> connector = new Db2ColumnConnector<Character>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<Character>(
          schema, table, column, type, isEnum, connector, op, p);
    } else {
      Parser<String> p = new StringParser();
      ColumnConnector<String> connector = new Db2ColumnConnector<String>(
          jdbcConnector, schema, table, column, p);
      Set<Constraint> constraints = connector.getConstraints();
      if (constraints.contains(Constraint.UNIQUE) ||
          constraints.contains(Constraint.PRIMARY_KEY)) {
        throw new UnsupportedTypeException(typeName, Constraint.UNIQUE);
      }
      profiler = new PseudoColumnProfiler(
          schema, table, column, type, isEnum, connector);
      LOGGER.warn(schema + "." + table + "." + column
          + " is not supported using pseudo profiler instead!");
    }
    return profiler;
  }

  public static ColumnProfiler<?> getProfilerOracle(final String schema, final  String table
	      , final String column, final TypeInfo type, final JdbcConnector jdbcConnector
	      , final MetaConnector metaConnector) throws SQLException {
  	LOGGER.debug("entering Oligos->getProfilerOracle ...");
	  ColumnProfiler<?> profiler = null;
	  String typeName = type.getTypeName().toLowerCase();
	  boolean isEnum = metaConnector.isEnumerated(schema, table, column);
	  if (typeName.equals("number")) {
	  	LOGGER.debug("column type is number with scale = " + type.getScale());
		  Parser<BigDecimal> p = new BigDecimalParser();
		  Operator<BigDecimal> op = new BigDecimalOperator();
		  ColumnConnector<BigDecimal> connector = new OracleColumnConnector<BigDecimal>(
		  										jdbcConnector, schema, table, column, BigDecimal.class, p); 
		  profiler = new ColumnProfiler<BigDecimal>(schema, table, column, type, isEnum, connector, op, p);
	  } 
	  else if (typeName.equals("date") || typeName.equals("timestamp")) {
	  		LOGGER.debug("column type is date");
	  		Parser<Timestamp> p = new TimestampParser();
	      Operator<Timestamp> op = new TimestampOperator();
	      ColumnConnector<Timestamp> connector = new OracleColumnConnector<Timestamp>(jdbcConnector, schema, table, column, Date.class, p); 
	    	profiler = new ColumnProfiler<Timestamp>(schema, table, column, type, isEnum, connector, op, p);
	  } 
	  else if (typeName.equals("char") || typeName.equals("nchar")) {
	  		LOGGER.debug("column type is char or nchar");
	  		Parser<Character> p = new CharParser();
	    	Operator<Character> op = new CharOperator();
	    	ColumnConnector<Character> connector = new OracleColumnConnector<Character>(
	    										jdbcConnector, schema, table, column, Character.class, p); 
	    	profiler = new ColumnProfiler<Character>(schema, table, column, type, isEnum, connector, op, p);
	  } 
	  else if (typeName.equals("varchar2") || typeName.equals("nvarchar2") || 
	  		typeName.equals("varchar") || typeName.equals("nvarchar")) {
	  		LOGGER.debug("column type is varchar2");
	  		Parser<String> p = new StringParser();
	  		//Operator<BigDecimal> op = new BigDecimalOperator();
	  		Operator<String> op = new StringOperator();
	    	ColumnConnector<String> connector = new OracleColumnConnector<String>(
	    										jdbcConnector, schema, table, column, String.class, p, type);
	    	Set<Constraint> constraints = connector.getConstraints();
	    	if (constraints.contains(Constraint.UNIQUE) || constraints.contains(Constraint.PRIMARY_KEY))
	    		throw new UnsupportedTypeException(typeName, Constraint.UNIQUE);
	    	profiler = new PseudoColumnProfiler(schema, table, column, type, isEnum, connector);
	    	//profiler = new ColumnProfiler<String>(schema, table, column, type, isEnum, connector, op, p);
	  }
	  else {
	  	LOGGER.fatal("unsupported column type: " + typeName);
	  }
	  LOGGER.debug("leaving Oligos->getProfilerOracle");
	  return profiler;
	 
  }
  
  public static void main(String[] args) throws TypeNotSupportedException {
  	BasicConfigurator.configure();
  
  	// TODO create cmdline option for setting logger level 
    LOGGER.setLevel(Level.ALL);
  	CommandLineInterface cli = new CommandLineInterface(args);
    try {
      // TODO hard exit if the parsing fails!
      // better catch exceptions and log them
      if (!cli.parse()) {
        System.exit(2);
      }
      
      Properties props = new Properties();
      props.setProperty("user", cli.getUsername());
      props.setProperty("password", cli.getPassword());      
      Connection connection = DriverManager.getConnection(
          cli.getConnectionString(), props);
      JdbcConnector jdbcConnector = new JdbcConnector(connection);
//      JdbcConnector jdbcConnector = cli.getJdbcConnector();
//      jdbcConnector.connect(cli.getUsername(), cli.getPassword());
      MetaConnector metaConnector = null;
      Driver dbDriver = cli.dbDriver;
      switch (dbDriver.driverName){
	  		case db2:
	  			LOGGER.trace("metaConnector = Db2MetaConnector");
	  			metaConnector = new Db2MetaConnector(jdbcConnector);
	  			break;
	  		case oracle:
	  			metaConnector = new OracleMetaConnector(jdbcConnector);
	  			break;
	  		default:
	  			LOGGER.error("Unknown database driver (see Driver.java for known drivers)");
      }
      
      // validating schema
      LOGGER.info("Validating input schema ...");
      SparseSchema sparseSchema = cli.getInputSchema();
      LOGGER.trace("User specified schema " + sparseSchema);
      DenseSchema inputSchema = DbUtils.populateSchema(sparseSchema, jdbcConnector, metaConnector);
      LOGGER.trace("Populated and validated schema " + inputSchema);
      
      // obtaining type information/ column meta data
      LOGGER.info("Retrieving column meta data ...");
      Map<ColumnId, TypeInfo> columnTypes = Maps.newLinkedHashMap();
      for (ColumnId columnId : inputSchema) {
      	TypeInfo type = metaConnector.getColumnType(columnId);
        columnTypes.put(columnId, type);
      }
      
    	// creating connectors and profilers
      LOGGER.info("Establashing database connection ...");
      SchemaConnector schemaConnector = null;
      TableConnector tableConnector = null;
      switch(dbDriver.driverName){
      case db2:
    	   schemaConnector = new Db2SchemaConnector(jdbcConnector);
    	   tableConnector = new Db2TableConnector(jdbcConnector);
    	   break;
      case oracle:
    	   schemaConnector = new OracleSchemaConnector(jdbcConnector);
    	   tableConnector = new OracleTableConnector(jdbcConnector);
      }
      Set<SchemaProfiler> profilers = Sets.newLinkedHashSet();
      for (String schema : inputSchema.schemas()) {
        SchemaProfiler schemaProfiler = new SchemaProfiler(schema,
            schemaConnector);
        profilers.add(schemaProfiler);
        for (String table : inputSchema.tablesIn(schema)) {
          TableProfiler tableProfiler = new TableProfiler(schema, table, tableConnector);
          schemaProfiler.add(tableProfiler);
          for (String column : inputSchema.columnsIn(schema, table)) {
            ColumnId columnId = new ColumnId(schema, table, column);
            TypeInfo type = columnTypes.get(columnId);
            ColumnProfiler<?> columnProfiler = null;
            switch(dbDriver.driverName){
            case db2:
                columnProfiler = getProfiler(schema, table, column, type, jdbcConnector, metaConnector);
                break;
            case oracle:
                columnProfiler = getProfilerOracle(schema, table, column, type, jdbcConnector, metaConnector);
            }
            tableProfiler.addColumnProfiler(columnProfiler);
          }
        }
      }
      // profiling statistical data
      LOGGER.info("Profiling schema ...");
      Set<Schema> profiledSchemas = Sets.newLinkedHashSet();
      for (SchemaProfiler schemaProfiler : profilers) {
        Schema profiledSchema = schemaProfiler.profile();
        profiledSchemas.add(profiledSchema);
      }
      
      LOGGER.info("Generating generator specification ...");
      File outputDir = cli.getOutputDirectory();
      String generatorName = cli.getGeneratorName();
      LOGGER.debug("Writing generator specification ...");
      for (Schema schema : profiledSchemas) {
        MyriadWriter writer = new MyriadWriter(schema, outputDir, generatorName);
        writer.write();
      }
      LOGGER.debug("Close JdbcConnector ...");
      jdbcConnector.close();
    } catch (SQLException e) {
      LOGGER.error(e.getLocalizedMessage());
      LOGGER.debug(ExceptionUtils.getStackTrace(e));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage());
      LOGGER.debug(ExceptionUtils.getStackTrace(e));
    } catch (ParseException e) {
      LOGGER.error(e.getMessage());
      cli.printHelpMessage();
    }
  }
  
}
