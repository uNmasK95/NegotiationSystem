package data;

import exception.UserNotFoundException;
import org.postgresql.util.PSQLException;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class Banco {

  public static final String host = "127.0.0.1";
  public static final int port = 12348;
  public static final String database = "banco";

  private Connection c;

  public Banco() throws SQLException {
   this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
  }

  public Banco(String host, int port, String database) throws SQLException {
    this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
  }

  public float getSaldo(String utilizador) throws UserNotFoundException {
    PreparedStatement s;
    ResultSet rs = null;
    float saldo;
    try {
      s = c.prepareStatement("" +
          "SELECT saldo FROM utilizadores " +
          "WHERE username = ?");
      s.setString(1,utilizador);
      rs = s.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      rs.next();
      saldo = rs.getFloat(1);
    }catch (SQLException e) {
      throw new UserNotFoundException(utilizador);
    }
    return saldo;
  }
}
