package data;

import exception.UserNotFoundException;

import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by pedro on 23-12-2016.
 */
public class Acoes {

  private Connection connection;

  public Acoes(Connection c){
    this.connection = c;
  }

  // FIXME - Estar sempre a chamar este metodo e' muito ineficiente
  public Set<String> getEmpresas(){
    TreeSet<String> acoes = new TreeSet<>();

    PreparedStatement s = null;
    ResultSet rs = null;
    try {
      s = connection.prepareStatement("SELECT DISTINCT empresa FROM acoes");
      rs = s.executeQuery();
      while (rs.next()) {
        acoes.add(rs.getString("empresa"));
      }
    }
    catch(SQLException e){
      e.printStackTrace();
    }
    finally {
      try {
        rs.close();
        s.close();
      } catch (SQLException e) {
      }
    }
    return acoes;
  }

  /*
  Retorna Map<Empresa,Quantidade_Acoes_Utilizador>
 */
  public Map<String,Integer> getAcoesUtilizador(String utilizador){
    TreeMap<String,Integer> acoes = new TreeMap<>();

    PreparedStatement s = null;
    ResultSet rs = null;
    try {
      s = connection.prepareStatement("" +
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
      } catch (SQLException e) {
      }
    }
    return acoes;
  }

  public int getAcoes(String empresa, String utilizador) throws UserNotFoundException {
    PreparedStatement s = null;
    ResultSet rs = null;
    int acoes;
    try {
      s = connection.prepareStatement("" +
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
      } catch (SQLException e) {
      }
    }
    return acoes;
  }
}
