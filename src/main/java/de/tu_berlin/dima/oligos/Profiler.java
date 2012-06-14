package de.tu_berlin.dima.oligos;

import java.sql.SQLException;
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

public class Profiler {

  private static final Logger LOGGER = Logger.getLogger(Profiler.class);
  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      // TODO delete this option, obtain password from scanner
      .addOption("pass", "password", true, "Password for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("t", "table", true, "Database table to profile")
      .addOption("p", "port", true, "Database port");
  private static final String USAGE = Profiler.class.getSimpleName() + " -u <user> -h <host> -d <database> -p <port> [column ...]";
  private static final String HEADER = Profiler.class.getSimpleName() + " is a application to infer statistical information from a database catalog.";

  private DB2Connector connector;

  public Profiler(DB2Connector connector) {
    this.connector = connector;
  }

  public void profileColumn(String table, String column) throws SQLException {
    @SuppressWarnings("rawtypes")
    Class type = connector.getColumnType(table, column);
    LOGGER.debug("Found type " + type + " for " + table + "." + column);
    profileColumn(table, column, type);
  }

  @SuppressWarnings("rawtypes")
  public void profileColumn(String table, String column, Class type)
      throws SQLException {
    LOGGER.info("Profiling " + table + "." + column);
    System.out.println(connector.profileColumn(table, column));
  }

  /**
   * @param args
   */
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
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("hostname")) {
        host = cmd.getOptionValue("hostname");
      } else {
        System.out
            .println("Please specify a hostname for the database connection");
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("database")) {
        db = cmd.getOptionValue("database");
      } else {
        System.out
            .println("Please specify a database for the database connection");
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
        return;
      }

      if (cmd.hasOption("port")) {
        port = Integer.parseInt(cmd.getOptionValue("port"));
      } else {
        System.out.println("Please specify a port for the database connection");
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
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
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
        return;
      }
      // TODO use this method to get the password
      // Scanner scanner = new Scanner(System.in);
      // pass = scanner.next();

      if (cmd.getArgs().length > 0) {
        columns = cmd.getArgs();
      } else {
        System.out.println("Please specify one or more database columns");
        formatter.printHelp(USAGE, HEADER, OPTS, "");
        return;
      }

      DB2Connector connector = new DB2Connector(host, db, port);
      connector.connect(user, pass);
      Profiler profiler = new Profiler(connector);
      for (String col : columns) {
        profiler.profileColumn(table, col);
      }

      connector.close();

    } catch (ParseException e) {
      formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
