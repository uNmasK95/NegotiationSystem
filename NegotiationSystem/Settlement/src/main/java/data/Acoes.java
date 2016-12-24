package data;

import exception.UserNotFoundException;

import java.sql.*;

/**
 * Created by pedro on 23-12-2016.
 */
public class Acoes {

  public static final String host = "127.0.0.1";
  public static final int port = 12347;
  public static final String database = "acoes";

  private Connection c;

  public Acoes() throws SQLException {
    this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
  }

  public Acoes(String host, int port, String database) throws SQLException {
    this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
  }

  public int getAcoes(String empresa, String utilizador) throws UserNotFoundException {
    PreparedStatement s;
    ResultSet rs = null;
    int acoes;
    try {
      s = c.prepareStatement("" +
          "SELECT quantidade FROM acoes " +
          "WHERE empresa = ? AND utilizador = ?");
      s.setString(1,empresa);
      s.setString(2,utilizador);
      rs = s.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      rs.next();
      acoes = rs.getInt(1);
    }catch (SQLException e) {
      return 0;
    }
    return acoes;
  }
}
