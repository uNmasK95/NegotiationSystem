package data;

import exception.InsuficientFundsException;
import exception.UserNotFoundException;

import java.sql.*;

/**
 * Created by pedro on 01-01-2017.
 */
public class Banco {

  public String host;
  public int port;
  public String database;

  private Connection c;

  public Banco(){
    this.host = "127.0.0.1";
    this.port = 12348;
    this.database = "banco";
  }

  public Banco(String host, int port, String database){
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

  public void transfer(String from, String to, float amount) throws InsuficientFundsException, UserNotFoundException {
    try {
      this.openConnection();
      c.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      //Verificar se utilizador "from" tem saldo suficiente
      PreparedStatement s1 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?");
      s1.setString(1,from);
      ResultSet rs1 = s1.executeQuery();
      float saldo_from = 0;
      if(rs1.next()){
        saldo_from = rs1.getFloat("saldo");
        if(saldo_from < amount){
          try{
            rs1.close();
            s1.close();
            c.close();
          }
          catch (Exception e){}
          throw new InsuficientFundsException(""+saldo_from);
        }
      }
      // utilizador "from" nao existe
      else{
        try{
          rs1.close();
          s1.close();
          c.close();
        }
        catch (Exception e){}
        throw new UserNotFoundException(""+from);
      }

      //Retirar saldo a "from"
      PreparedStatement s2 = c.prepareStatement("" +
          "update utilizadores set saldo = saldo - ? " +
          "where username = ?");
      s2.setFloat(1,amount);
      s2.setString(2,from);
      s2.executeUpdate();
      s2.close();

      //Creditar saldo no utilizador "to"
        // Verificar se utilizador "to" existe
      PreparedStatement s3 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?"
      );
      s3.setString(1,to);
      ResultSet rs3 = s3.executeQuery();
      if(rs3.next()){
        // Utilizador "to" existe
        PreparedStatement s4 = c.prepareStatement("" +
            "update utilizadores set saldo = saldo + ? " +
            "where username = ?"
        );
        s4.setFloat(1, amount);
        s4.setString(2, to);
        s4.executeUpdate();
        s4.close();
      }
      else{
        // utilizador "to" nao existe
        try{
          rs3.close();
          s3.close();
          c.close();
        }
        catch (Exception e){}
        throw new UserNotFoundException(""+to);
      }
      rs3.close();
      s3.close();

      c.commit();
      c.setAutoCommit(true);
    }
    catch (SQLException e){
      try{
        c.rollback();
      }
      catch (Exception e2){
        e2.printStackTrace();
      }
    }
    finally {
      try {
        c.close();
      }
      catch (Exception e){}
    }
  }
}
