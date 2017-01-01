package data;

import exception.UserNotFoundException;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by pedro on 23-12-2016.
 */
public class Acoes {

  public String host = "127.0.0.1";
  public int port = 12347;
  public String database = "acoes";

  private Connection c;

  public Acoes(){
    this.host = "127.0.0.1";
    this.port = 12347;
    this.database = "acoes";
  }

  public Acoes(String host, int port, String database){
    this.host = host;
    this.port = port;
    this.database = database;
  }

  private void openConnection() throws SQLException {
    this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
  }

  private void closeConection() throws SQLException {
    this.c.close();
  }

  /*
    Retorna Map<Empresa,Quantidade_Acoes>
   */
  public Map<String,Integer> getAcoesUtilizador(String utilizador){
    TreeMap<String,Integer> acoes = new TreeMap<>();

    try { this.openConnection(); } catch (SQLException e) { e.printStackTrace(); return null; }

    PreparedStatement s = null;
    ResultSet rs = null;
    try {
      s = c.prepareStatement("" +
          "SELECT empresa, quantidade FROM acoes " +
          "WHERE utilizador = ?");
      s.setString(1, utilizador);
      rs = s.executeQuery();
      while (rs.next()) {
        acoes.put(rs.getString("empresa"),rs.getInt("quantidade"));
      }
    }
    catch(SQLException e){
      e.printStackTrace();
    }
    finally {
      try {
        rs.close();
        s.close();
        c.close();
      } catch (SQLException e) {
      }
    }
    return acoes;
  }

  public int getAcoes(String empresa, String utilizador) throws UserNotFoundException {
    PreparedStatement s = null;
    ResultSet rs = null;
    int acoes;
    try { openConnection(); } catch (SQLException e) {e.printStackTrace(); return 0;}
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
    finally {
      try {
        rs.close();
        s.close();
        c.close();
      } catch (SQLException e) {
      }
    }
    return acoes;
  }
}
