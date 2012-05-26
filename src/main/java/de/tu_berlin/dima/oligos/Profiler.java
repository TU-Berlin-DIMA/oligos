package de.tu_berlin.dima.oligos;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import de.tu_berlin.dima.oligos.histogram.QHist;

public class Profiler {

  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("p", "port", true, "Database port");

  /**
   * @param args
   */
  public static void main(String[] args) {
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    HelpFormatter formatter = new HelpFormatter();

    try {
      cmd = parser.parse(OPTS, args);
      String user;
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
      // Scanner scanner = new Scanner(System.in);
      // String pass = scanner.next();
      String pass = "db2inst";

      DB2Connector connector = new DB2Connector(host, db, port);
      connector.connect(user, pass);
      QHist<Date> shipDateQHist = connector.getDateQHistFor("lineitem",
          "l_shipdate");
      System.out.println("Q Hist");
      for (int i = 0; i < shipDateQHist.getNumBuckets(); i++) {
        String str = shipDateQHist.getUpperBoundAt(i) + "\t"
            + shipDateQHist.getFrequencyAt(i);
        System.out.println(str);
      }
      Map<Date, Integer> shipDateFHist = connector.getDateFHistFor("lineitem",
          "l_shipdate");
      System.out.println("F Hist");
      for (Entry<Date, Integer> e : shipDateFHist.entrySet()) {
        String str = e.getKey() + "\t" + e.getValue();
        System.out.println(str);
      }

    } catch (ParseException e) {
      formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
