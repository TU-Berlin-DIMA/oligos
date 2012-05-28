package de.tu_berlin.dima.oligos;

import java.sql.SQLException;
import java.util.Calendar;
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
import org.joda.time.DateTime;
import org.joda.time.Days;

import de.tu_berlin.dima.oligos.db.DB2Connector;
import de.tu_berlin.dima.oligos.histogram.AdaptiveQHist;
import de.tu_berlin.dima.oligos.histogram.FHist;
import de.tu_berlin.dima.oligos.histogram.Operator;
import de.tu_berlin.dima.oligos.histogram.QHist;

public class Profiler {

  private static final Logger LOGGER = Logger.getLogger(Profiler.class);
  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("d", "database", true, "Use given database")
      .addOption("p", "port", true, "Database port");

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

      // obtain the quantile histogram
      QHist<Date> shipDateQHist = connector.getDateQHistFor("lineitem",
          "l_shipdate");
      LOGGER.debug(shipDateQHist);

      // obtain the N most frequent values (together with their frequencies)
      FHist<Date> shipDateFHist = connector.getDateFHistFor("lineitem",
          "l_shipdate");
      LOGGER.debug(shipDateFHist);

      Operator<Date> op = new Operator<Date>() {

        private Calendar cal = Calendar.getInstance();

        @Override
        public Date increment(Date value) {
          cal.setTime(value);
          cal.add(Calendar.DATE, 1);
          return cal.getTime();
        }

        @Override
        public Date decrement(Date value) {
          cal.setTime(value);
          cal.add(Calendar.DATE, -1);
          return cal.getTime();
        }

        @Override
        public int difference(Date val1, Date val2) {
          DateTime dt1 = new DateTime(val1);
          DateTime dt2 = new DateTime(val2);
          return Days.daysBetween(dt1, dt2).getDays();
        }
      };

      AdaptiveQHist<Date> adaptQHist = new AdaptiveQHist<Date>(shipDateQHist,
          shipDateFHist, op);
      LOGGER.debug(adaptQHist);

      connector.close();

    } catch (ParseException e) {
      formatter.printHelp(Profiler.class.getSimpleName(), OPTS);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
