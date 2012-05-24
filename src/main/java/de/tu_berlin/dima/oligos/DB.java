package de.tu_berlin.dima.oligos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {

  public static void main(String... args) {

    try {
      Class.forName("com.ibm.db2.jcc.DB2Driver");
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String url = "jdbc:db2://koala.dima.cs.tu-berlin.de:60004/imdb";
    Connection conn = null;
    PreparedStatement prepStmt = null;
    String query = "SELECT * FROM title FETCH FIRST 10 ROWS ONLY";
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
      
      while (result.next()) {
        System.out.println(result.getString("title"));
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
}
