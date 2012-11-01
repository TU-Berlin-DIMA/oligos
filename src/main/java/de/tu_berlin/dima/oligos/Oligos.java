package de.tu_berlin.dima.oligos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
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

import de.tu_berlin.dima.oligos.db.DB2Connector;
import de.tu_berlin.dima.oligos.exception.ColumnDoesNotExistException;
import de.tu_berlin.dima.oligos.exception.TypeNotSupportedException;
import de.tu_berlin.dima.oligos.io.MyriadWriter;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.type.util.TypeInfo;
import de.tu_berlin.dima.oligos.type.util.operator.CharOperator;
import de.tu_berlin.dima.oligos.type.util.operator.DateOperator;
import de.tu_berlin.dima.oligos.type.util.operator.DecimalOperator;
import de.tu_berlin.dima.oligos.type.util.operator.IntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.OperatorManager;
import de.tu_berlin.dima.oligos.type.util.parser.CharParser;
import de.tu_berlin.dima.oligos.type.util.parser.DateParser;
import de.tu_berlin.dima.oligos.type.util.parser.DecimalParser;
import de.tu_berlin.dima.oligos.type.util.parser.IntegerParser;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;
import de.tu_berlin.dima.oligos.type.util.parser.ParserManager;

public class Oligos {

  private static final Logger LOGGER = Logger.getLogger(Oligos.class);
  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      // TODO delete this option, obtain password from scanner
      .addOption("pass", "password", true, "Password for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("t", "table", true, "Database table to profile")
      .addOption("p", "port", true, "Database port")
      .addOption("o", "output", true, "Path to the output folder")
      .addOption("g", "generator", true, "Name of the generator");
  private static final String USAGE = Oligos.class.getSimpleName()
      + " -u <user> -h <host> -d <database> -p <port> [column ...]";
  private static final String HEADER = Oligos.class.getSimpleName()
      + " is a application to infer statistical information from a database catalog.";

  private DB2Connector connector;

  public Oligos(DB2Connector connector) {
    this.connector = connector;
  }
  
  public ColumnProfiler<?> getProfiler(String schema, String table, String column)
      throws SQLException, TypeNotSupportedException {
    ColumnProfiler<?> profiler = null;
    ParserManager parserManager = ParserManager.getInstance();
    OperatorManager operatorManager =  OperatorManager.getInstance();
    TypeInfo type = connector.getColumnType(table, column);
    String typeName = type.getTypeName().toLowerCase();
    if (typeName.equals("integer")) {
      Parser<Integer> p = new IntegerParser();
      Operator<Integer> op = new IntegerOperator();
      parserManager.register(schema, table, column, p);
      operatorManager.register(schema, table, column, op);
      profiler = new ColumnProfiler<Integer>(connector, p, op, table, column, typeName);
    } else if (typeName.equals("date")) {
      Parser<Date> p = new DateParser();
      Operator<Date> op = new DateOperator();
      parserManager.register(schema, table, column, p);
      operatorManager.register(schema, table, column, op);
      profiler = new ColumnProfiler<Date>(connector, p, op, table, column, typeName);
    } else if (typeName.equals("decimal")) {
      Parser<BigDecimal> p = new DecimalParser();
      Operator<BigDecimal> op = new DecimalOperator(type.getScale());
      parserManager.register(schema, table, column, p);
      operatorManager.register(schema, table, column, op);
      profiler = new ColumnProfiler<BigDecimal>(connector, p, op, table, column, typeName);
    } else if (typeName.equals("character")
        && (type.getLength() == 1)) {
      Parser<Character> p = new CharParser();
      Operator<Character> op = new CharOperator();
      parserManager.register(schema, table, column, p);
      operatorManager.register(schema, table, column, op);
      profiler = new ColumnProfiler<Character>(connector, p, op, table, column, typeName);
    } else {
      LOGGER.warn("Could not profile " + table + "." + column);
      throw new TypeNotSupportedException(typeName, type.getLength());
    }
    return profiler;
  }

  public Column<?> profile(String schema, String table, String column)
      throws ColumnDoesNotExistException, SQLException, TypeNotSupportedException {
    if (!connector.checkColumn(table, column)) {
      // TODO Use log4j or throw exception
      throw new ColumnDoesNotExistException(table, column);
    }
    if (connector.hasStatistics(table, column)) {
      ColumnProfiler<?> profiler = getProfiler(schema, table, column);
      return profiler.profile();
    } else {
      return null;
    }
  }

  public static void main(String[] args) {
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
        String schema = "";
        String table = cmd.getOptionValue("table");

        // get the columns
        String[] columns = cmd.getArgs();

        // actually run the profiling
        DB2Connector connector = new DB2Connector(hostname, database, port);
        connector.connect(username, password);
        Oligos profiler = new Oligos(connector);
        Set<Column<?>> profiledColumns = Sets.newLinkedHashSet();
        for (String col : columns) {
          try {
            Column<?> column = profiler.profile(schema, table, col);
            if (column != null) {
              profiledColumns.add(column);
            }
          } catch (ColumnDoesNotExistException cdnee) {
            System.err.println("Column " + cdnee.getQualifiedColumnName()
                + " does not exist");
          } catch (TypeNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        connector.close();
        
        Map<String, Set<Column<?>>> relations = Maps.newHashMap();
        relations.put(table, profiledColumns);
        MyriadWriter writer = new MyriadWriter(relations, outputDir, generatorName);
        try {
          writer.write();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } catch (ParseException e) {
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
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
    if (!cmd.hasOption("table")) {
      System.out.println("Please specify a database table");
      formatter.printHelp(USAGE, HEADER, OPTS, "");
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
