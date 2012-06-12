package de.tu_berlin.dima.oligos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
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
import de.tu_berlin.dima.oligos.histogram.CombinedHist;
import de.tu_berlin.dima.oligos.histogram.FHist;
import de.tu_berlin.dima.oligos.histogram.QHist;
import de.tu_berlin.dima.oligos.type.Parser;
import de.tu_berlin.dima.oligos.type.db2.DateOperator;
import de.tu_berlin.dima.oligos.type.db2.DateParser;
import de.tu_berlin.dima.oligos.type.db2.DecimalOperator;
import de.tu_berlin.dima.oligos.type.db2.DecimalParser;
import de.tu_berlin.dima.oligos.type.db2.DoubleOperator;
import de.tu_berlin.dima.oligos.type.db2.DoubleParser;
import de.tu_berlin.dima.oligos.type.db2.IntegerOperator;
import de.tu_berlin.dima.oligos.type.db2.IntegerParser;

public class Profiler {

  private static final Logger LOGGER = Logger.getLogger(Profiler.class);
  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      // TODO delete this option, obtain password from scanner
      .addOption("pass", "password", true, "Password for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("p", "port", true, "Database port");

  private DB2Connector connector;

  public Profiler(DB2Connector connector) {
    this.connector = connector;
  }
  
  public void profileColumn(String table, String column) throws SQLException {
    CombinedHist<?> combHist = connector.<Integer> profileColumn(table, column);
    System.out.println(combHist.toString());
  }

  public void profileColumn(String table, String column, Class<?> type)
      throws SQLException {
    System.out.println(table + "." + column);
    if (type.equals(Integer.class)) {
      Parser<Integer> parser = new IntegerParser();
      FHist<Integer> fHist = connector.getFHistFor(table, column, parser);
      QHist<Integer> qHist = connector.getQHistFor(table, column, parser,
          new IntegerOperator(), Integer.class);
      CombinedHist<Integer> combHist = new CombinedHist<Integer>(qHist, fHist,
          parser);
      System.out.println(combHist);
    } else if (type.equals(Double.class)) {
      Parser<Double> parser = new DoubleParser();
      FHist<Double> fHist = connector.getFHistFor(table, column, parser);
      QHist<Double> qHist = connector.getQHistFor(table, column, parser,
          new DoubleOperator(0.01), Double.class);
      CombinedHist<Double> combHist = new CombinedHist<Double>(qHist, fHist,
          parser);
      System.out.println(combHist);
    } else if (type.equals(Date.class)) {
      Parser<Date> parser = new DateParser("yyyy-MM-dd", "yyyy-MM-dd");
      FHist<Date> fHist = connector.getFHistFor(table, column, parser);
      QHist<Date> qHist = connector.getQHistFor(table, column, parser,
          new DateOperator(), Date.class);
      CombinedHist<Date> combHist = new CombinedHist<Date>(qHist, fHist, parser);
      System.out.println(combHist);
    } else if (type.equals(BigDecimal.class)) {
      Parser<BigDecimal> parser = new DecimalParser(2);
      FHist<BigDecimal> fHist = connector.getFHistFor(table, column, parser);
      QHist<BigDecimal> qHist = connector.getQHistFor(table, column, parser,
          new DecimalOperator(new BigDecimal(0.01)), BigDecimal.class);
      CombinedHist<BigDecimal> combHist = new CombinedHist<BigDecimal>(qHist,
          fHist, parser);
      System.out.println(combHist);
    } else {
      throw new IllegalArgumentException(type.getCanonicalName()
          + " is not a supported type.");
    }
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
      String user;
      String pass;
      String host;
      String db;
      int port;

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
      
      // TODO delete this option
      if (cmd.hasOption("password")) {
        pass = cmd.getOptionValue("password");
      } else {
        System.out.println("Please specify a password for the database connection");
        formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
        return;
      }
      // TODO use this method to get the password
      // Scanner scanner = new Scanner(System.in);
      // pass = scanner.next();

      DB2Connector connector = new DB2Connector(host, db, port);
      connector.connect(user, pass);
      LOGGER.debug(connector.getColumnType("ORDERS", "o_totalprice"));
      Profiler profiler = new Profiler(connector);
      // collect statistics for ORDERS relation
      // profiler.profileColumn("ORDERS", "O_ORDERKEY", Integer.class); //
      // Integer
      profiler.profileColumn("ORDERS", "O_CUSTKEY", Integer.class); // Integer
      profiler.profileColumn("ORDERS", "O_CUSTKEY"); // Integer
      // profiler.profileColumn("ORDERS", "O_ORDERSTATUS"); // Character(1)
      profiler.profileColumn("ORDERS", "O_TOTALPRICE", BigDecimal.class); // Decimal
      profiler.profileColumn("ORDERS", "O_ORDERDATE", Date.class); // Date
      // profiler.profileColumn("ORDERS", "O_ORDERPRIORITY"); // Character(15)
      // profiler.profileColumn("ORDERS", "O_CLERK"); // Character(15)
      // profiler.profileColumn("ORDERS", "O_SHIPPRIORITY", Integer.class); //
      // Integer
      // profiler.profileColumn("ORDERS", "O_COMMENT"); // Varchar(79)

      connector.close();

    } catch (ParseException e) {
      formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public enum Type {
    Integer, Double, Decimal, Date

  }
}
