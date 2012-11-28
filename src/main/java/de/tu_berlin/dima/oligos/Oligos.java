package de.tu_berlin.dima.oligos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.ColumnConnector;
import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.MetaConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2ColumnConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2MetaConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2SchemaConnector;
import de.tu_berlin.dima.oligos.db.db2.Db2TableConnector;
import de.tu_berlin.dima.oligos.exception.TypeNotSupportedException;
import de.tu_berlin.dima.oligos.io.MyriadWriter;
import de.tu_berlin.dima.oligos.profiler.ColumnProfiler;
import de.tu_berlin.dima.oligos.profiler.PseudoColumnProfiler;
import de.tu_berlin.dima.oligos.profiler.SchemaProfiler;
import de.tu_berlin.dima.oligos.profiler.TableProfiler;
import de.tu_berlin.dima.oligos.stat.Schema;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.operator.CharOperator;
import de.tu_berlin.dima.oligos.type.util.operator.DateOperator;
import de.tu_berlin.dima.oligos.type.util.operator.DecimalOperator;
import de.tu_berlin.dima.oligos.type.util.operator.IntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.parser.CharParser;
import de.tu_berlin.dima.oligos.type.util.parser.DateParser;
import de.tu_berlin.dima.oligos.type.util.parser.DecimalParser;
import de.tu_berlin.dima.oligos.type.util.parser.IntegerParser;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;
import de.tu_berlin.dima.oligos.type.util.parser.StringParser;

public class Oligos {

  private static final Logger LOGGER = Logger.getLogger(Oligos.class);
  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      // TODO delete this option, obtain password from scanner
      .addOption("pass", "password", true, "Password for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("p", "port", true, "Database port")
      .addOption("o", "output", true, "Path to the output folder")
      .addOption("g", "generator", true, "Name of the generator");
  private static final String USAGE = Oligos.class.getSimpleName()
      + " -u <user> -h <host> -d <database> -p <port> column [column]";
  private static final String HEADER = Oligos.class.getSimpleName()
      + " is a application to infer statistical information from a database catalog.";

  public static boolean isIncremetalType(TypeInfo type) {
    @SuppressWarnings("serial")
    Set<String> types = new HashSet<String>() {{
      add("integer");
      add("decimal");
      add("date");
    }};
    if (types.contains(type.getTypeName().toLowerCase())) {
      return true;
    } else {
      return false;
    }
  }

  public static InputSchema validateSchema(final InputSchema input
      , final MetaConnector connector) throws SQLException {
    InputSchema validatedSchema = new InputSchema();
    for (ColumnId columnId : input) {
      if (connector.hasColumn(columnId)) {
        validatedSchema.addColumn(columnId);
      } else {
        LOGGER.warn(columnId.getQualifiedName() + " does not exist!");
      }
    }
    return validatedSchema;
  }

  public static ColumnProfiler<?> getProfiler(final ColumnId columnId, final TypeInfo type
      , final JdbcConnector jdbcConnector, final MetaConnector metaConnector)
          throws SQLException, TypeNotSupportedException {
    String schema = columnId.getSchema();
    String table = columnId.getTable();
    String column = columnId.getColumn();
    return getProfiler(schema, table, column, type, jdbcConnector, metaConnector);
  }
  
  public static ColumnProfiler<?> getProfiler(final String schema, final  String table
      , final String column, final TypeInfo type, final JdbcConnector jdbcConnector
      , final MetaConnector metaConnector)
      throws SQLException, TypeNotSupportedException {
    ColumnProfiler<?> profiler = null;
    String typeName = type.getTypeName().toLowerCase();
    boolean isEnum = metaConnector.isEnumerated(schema, table, column);
    if (typeName.equals("integer")) {
      Parser<Integer> p = new IntegerParser();
      Operator<Integer> op = new IntegerOperator();
      ColumnConnector<Integer> connector = new Db2ColumnConnector<Integer>(
          jdbcConnector, schema, table, column, p);      
      profiler = new ColumnProfiler<Integer>(
          schema, table, column, typeName, isEnum, connector, op, p);
    } else if (typeName.equals("date")) {
      Parser<Date> p = new DateParser();
      Operator<Date> op = new DateOperator();
      ColumnConnector<Date> connector = new Db2ColumnConnector<Date>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<Date>(
          schema, table, column, typeName, isEnum, connector, op, p);
    } else if (typeName.equals("decimal")) {
      Parser<BigDecimal> p = new DecimalParser();
      Operator<BigDecimal> op = new DecimalOperator(type.getScale());
      ColumnConnector<BigDecimal> connector = new Db2ColumnConnector<BigDecimal>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<BigDecimal>(
          schema, table, column, typeName, isEnum, connector, op, p);
    } else if (typeName.equals("character")
        && (type.getLength() == 1)) {
      Parser<Character> p = new CharParser();
      Operator<Character> op = new CharOperator();
      ColumnConnector<Character> connector = new Db2ColumnConnector<Character>(
          jdbcConnector, schema, table, column, p); 
      profiler = new ColumnProfiler<Character>(
          schema, table, column, typeName, isEnum, connector, op, p);
    } else {
      LOGGER.warn(schema + "." + table + "." + column
          + " is not supported using pseudo profiler instead!");
      Parser<String> p = new StringParser();
      ColumnConnector<String> connector = new Db2ColumnConnector<String>(
          jdbcConnector, schema, table, column, p);
      profiler = new PseudoColumnProfiler(
          schema, table, column, typeName, isEnum, connector);
    }
    return profiler;
  }

  public static void main(String[] args) throws TypeNotSupportedException {
    BasicConfigurator.configure();
    LOGGER.setLevel(Level.ALL);
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    HelpFormatter formatter = new HelpFormatter();

    try {
      cmd = parser.parse(OPTS, args);
      if (checkOptions(cmd, formatter)) {
        // get database credentials
        String username = cmd.getOptionValue("username");
        String hostname = cmd.getOptionValue("hostname");
        int port = Integer.parseInt(cmd.getOptionValue("port"));
        // TODO delete this option
        String password = cmd.getOptionValue("password");
        // get output information
        File outputDir = new File(cmd.getOptionValue("output"));
        String generatorName = cmd.getOptionValue("generator");
        // TODO use this method to get the password
        // Scanner scanner = new Scanner(System.in);
        // pass = scanner.next();
        // get profiling information
        String database = cmd.getOptionValue("database");
        // TODO get this from user
        //String schemaName = "DB2INST2";
        //String table = cmd.getOptionValue("table");

        // get the columns
        InputSchema inputSchema = new InputSchema();
        String[] columns = cmd.getArgs();
        for (String col : columns) {
          System.out.println(col);
          String[] ref = col.split("\\.");
          if (ref.length == 3) {
            String schema = ref[0];
            String table = ref[1];
            String column = ref[2];
            inputSchema.addColumn(schema, table, column);
          } else {
            LOGGER.error("Fully qualified column names");
          }
        }

        JdbcConnector jdbcConnector = new JdbcConnector(hostname, port, database
            , JdbcConnector.IBM_JDBC_V4);
        jdbcConnector.connect(username, password);
        MetaConnector metaConnector = new Db2MetaConnector(jdbcConnector);
        // validating schema
        LOGGER.info("Validating input schema ...");
        InputSchema validatedSchema = validateSchema(inputSchema, metaConnector);

        // obtaining type information/ column meta data
        LOGGER.info("Retrieving column meta data ...");
        Map<ColumnId, TypeInfo> columnTypes = Maps.newLinkedHashMap();
        for (ColumnId columnId : validatedSchema) {
          TypeInfo type = metaConnector.getColumnType(columnId);
          columnTypes.put(columnId, type);
        }
        
        // creating connectors and profilers
        LOGGER.info("Establashing database connection ...");
        SchemaConnector schemaConnector = new Db2SchemaConnector(jdbcConnector);
        TableConnector tableConnector = new Db2TableConnector(jdbcConnector);
        Set<SchemaProfiler> profilers = Sets.newLinkedHashSet();
        for (String schema : validatedSchema.schemas()) {
          SchemaProfiler schemaProfiler = new SchemaProfiler(schema, schemaConnector);
          profilers.add(schemaProfiler);
          for (String table : validatedSchema.tables(schema)) {
            TableProfiler tableProfiler = new TableProfiler(schema, table, tableConnector);
            schemaProfiler.add(tableProfiler);
            for (String column : validatedSchema.columns(schema, table)) {
              ColumnId columnId = new ColumnId(schema, table, column);
              TypeInfo type = columnTypes.get(columnId);
              ColumnProfiler<?> columnProfiler =
                  getProfiler(columnId, type, jdbcConnector, metaConnector);
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
        for (Schema schema : profiledSchemas) {
          MyriadWriter writer = new MyriadWriter(schema, outputDir, generatorName);
          writer.write();
        }
        
        jdbcConnector.close();
      }
    } catch (ParseException e) {
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static boolean checkOptions(CommandLine cmd, HelpFormatter formatter) {
    if (cmd.hasOption("help")) {
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    if (!cmd.hasOption("username")) {
      System.out
          .println("Please specify a username for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("hostname")) {
      System.out
          .println("Please specify a hostname for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("database")) {
      System.out
          .println("Please specify a database for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("port")) {
      System.out.println("Please specify a port for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("generator")) {
      System.out.println("Please specify data generator name");
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    if (!cmd.hasOption("output")) {
      System.out.println("Please specify output directory");
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    // TODO delete this option
    if (!cmd.hasOption("password")) {
      System.out
          .println("Please specify a password for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    
    return true;
  }
}
