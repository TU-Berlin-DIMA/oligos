package de.tu_berlin.dima.oligos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import de.tu_berlin.dima.oligos.histogram.QHist;

public class DB2QHistShipDate {
  public static void main(String... args) {

    try {
      Class.forName("com.ibm.db2.jcc.DB2Driver");
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String url = "jdbc:db2://koala.dima.cs.tu-berlin.de:60004/tpch";
    Connection conn = null;
    PreparedStatement prepStmt = null;
    String query = "SELECT * " + "FROM SYSSTAT.COLDIST "
        + "WHERE COLNAME = 'L_SHIPDATE' AND  TYPE ='Q' " + "ORDER BY SEQNO";
    ResultSet result = null;

    try {
      conn = DriverManager.getConnection(url, "db2inst2", "db2inst");
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      prepStmt = conn.prepareStatement(query);
      result = prepStmt.executeQuery();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      List<Date> dates = new ArrayList<Date>();
      List<Integer> frequencies = new ArrayList<Integer>();

      while (result.next()) {
        Date date = dateFormat.parse(result.getString("COLVALUE").replaceAll("\'", ""));
        frequencies.add(result.getInt("VALCOUNT"));
        dates.add(date);
        System.out.println(date);
      }
      
      Comparable<Date>[] bounds = dates.toArray(new Comparable[0]);
      int[] freqs = ArrayUtils.toPrimitive(frequencies.toArray(new Integer[0]));
      QHist hist = new QHist(bounds, freqs);
      System.out.println(hist.getCumFrequencyOf(dateFormat.parse("1992-02-06")));
      System.out.println(hist.getCumFrequencyOf(dateFormat.parse("1998-02-16")));
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
