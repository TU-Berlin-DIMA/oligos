package de.tu_berlin.dima.oligos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.tu_berlin.dima.oligos.db.DB2Connector;
import de.tu_berlin.dima.oligos.type.util.Constraint;
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
      .addOption("o", "output", true, "Path to the output folder");
  private static final String USAGE = Oligos.class.getSimpleName()
      + " -u <user> -h <host> -d <database> -p <port> [column ...]";
  private static final String HEADER = Oligos.class.getSimpleName()
      + " is a application to infer statistical information from a database catalog.";

  private DB2Connector connector;

  public Oligos(DB2Connector connector) {
    this.connector = connector;
  }

  public void profileColumn(String table, String column) throws SQLException {
    if (connector.checkColumn(table, column)) {
      TypeInfo type = connector.getColumnType(table, column);
      boolean isEnumerated = connector.isEnumerated(table, column);
      boolean hasStatistics = connector.hasStatistics(table, column);
      Set<Constraint> constraint = connector
          .getColumnConstraints(table, column);
      System.out.println(column + ": " + type);
      System.out.println("enumerated: " + isEnumerated + ", statistics: "
          + hasStatistics + ", constraints: " + constraint);
      ColumnProfiler<?> profiler = null;
      if (hasStatistics && !isEnumerated) {
        if (type.getTypeName().equalsIgnoreCase("integer")) {
          Parser<Integer> p = IntegerParser.getInstance();
          Operator<Integer> op = IntegerOperator.getInstance();
          profiler = new ColumnProfiler<Integer>(connector, p, op, table,
              column);
        } else if (type.getTypeName().equalsIgnoreCase("date")) {
          Parser<Date> p = DateParser.getInstance();
          Operator<Date> op = DateOperator.getInstance();
          profiler = new ColumnProfiler<Date>(connector, p, op, table, column);
        } else if (type.getTypeName().equalsIgnoreCase("decimal")) {
          Parser<BigDecimal> p = DecimalParser.getInstance();
          Operator<BigDecimal> op = new DecimalOperator(type.getScale());
          profiler = new ColumnProfiler<BigDecimal>(connector, p, op, table,
              column);
        } else if (type.getTypeName().equalsIgnoreCase("character")
            && (type.getLength() == 1)) {
          Parser<Character> p = new CharParser();
          Operator<Character> op = new CharOperator();
          profiler = new ColumnProfiler<Character>(connector, p, op, table,
              column);
        } else {
          return;
        }
        profiler.profile();
      } else if (isEnumerated) {
        Map<String, Long> mostFrequent = connector.getMostFrequentValues(table,
            column);
        for (Entry<String, Long> e : mostFrequent.entrySet()) {
          String value = e.getKey().replace('\'', ' ').trim();
          System.out.println(value + "\t" + e.getValue());
        }
      } else {
        System.out.println(table + "." + column + " has no statistics!");
      }
    } else {
      System.err.println(table + "." + column + " does not exist!");
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
      String user, pass, host, db, table;
      int port;
      String[] columns;

      if (cmd.hasOption("help")) {
        formatter.printHelp(USAGE, HEADER, OPTS, "");
        return;
      }

      if (cmd.hasOption("username")) {
        user = cmd.getOptionValue("username");
      } else {
        System.out
            .println("Please specify a username for the database connection");
        formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("hostname")) {
        host = cmd.getOptionValue("hostname");
      } else {
        System.out
            .println("Please specify a hostname for the database connection");
        formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("database")) {
        db = cmd.getOptionValue("database");
      } else {
        System.out
            .println("Please specify a database for the database connection");
        formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("port")) {
        port = Integer.parseInt(cmd.getOptionValue("port"));
      } else {
        System.out.println("Please specify a port for the database connection");
        formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("table")) {
        table = cmd.getOptionValue("table");
      } else {
        System.out.println("Please specify a database table");
        formatter.printHelp(USAGE, HEADER, OPTS, "");
        return;
      }

      // TODO delete this option
      if (cmd.hasOption("password")) {
        pass = cmd.getOptionValue("password");
      } else {
        System.out
            .println("Please specify a password for the database connection");
        formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
        return;
      }
      // TODO use this method to get the password
      // Scanner scanner = new Scanner(System.in);
      // pass = scanner.next();

      // get the columns
      if (cmd.getArgs().length > 0) {
        columns = cmd.getArgs();
      } else {
        System.out.println("Please specify one or more database columns");
        formatter.printHelp(USAGE, HEADER, OPTS, "");
        return;
      }

      // actually run the profiling
      DB2Connector connector = new DB2Connector(host, db, port);
      connector.connect(user, pass);
      Oligos profiler = new Oligos(connector);
      for (String col : columns) {
        profiler.profileColumn(table, col);

      }

      connector.close();

    } catch (ParseException e) {
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
