package data;

import java.sql.*;

public class BancoConnection {

  private String host;
  private int port;
  private String database;
  private Connection c;

  public BancoConnection(){
    this.host = "127.0.0.1";
    this.port = 12348;
    this.database = "banco";
  }

  public BancoConnection(String host, int port, String database){
    this.host = host;
    this.port = port;
    this.database = database;
  }

  private void openConnection(boolean autoCommit) throws SQLException {
    this.c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database);
    this.c.setAutoCommit(autoCommit);
  }

  private void closeConection() throws SQLException {
    this.c.close();
  }

  public void transfer(String from, String to, float amount){
    try {
      this.openConnection(false);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      //Verificar se utilizador "from" existe
      PreparedStatement s1 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?");
      s1.setString(1,from);
      ResultSet rs1 = s1.executeQuery();
      if(!rs1.next()){
        //Utilizador "from" nao existe. Criar nova entrada
        PreparedStatement s1_a = c.prepareStatement("" +
            "insert into utilizadores (username,saldo) values " +
            "(?,?);"
        );
        s1_a.setString(1,from);
        s1_a.setFloat(2,-amount);
        s1_a.executeUpdate();
        s1_a.close();
      }
      else{
        //"from" existe
        PreparedStatement s1_b = c.prepareStatement("" +
            "update utilizadores set saldo = saldo - ? where username = ?");
        s1_b.setFloat(1,amount);
        s1_b.setString(2,from);
        s1_b.executeUpdate();
        s1_b.close();
      }
      rs1.close();
      s1.close();

      //Creditar saldo em "to"
      // Verificar se ja existe um registo para o utilizador "to"
      PreparedStatement s3 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?"
      );
      s3.setString(1,to);
      ResultSet rs3 = s3.executeQuery();
      if(rs3.next()){
        // Ja existe um registo
        // Atualizar saldo
        PreparedStatement s4 = c.prepareStatement("" +
            "update utilizadores set saldo = saldo + ? " +
            "where username = ?"
        );
        s4.setFloat(1, amount);
        s4.setString(2, to);
        s4.executeUpdate();
        s4.close();
      }
      else {
        // "to" nao existe. Criar nova entrada
        PreparedStatement s5 = c.prepareStatement("" +
            "insert into utilizadores (username,saldo) values " +
            "(?,?);"
        );
        s5.setString(1,to);
        s5.setFloat(2,amount);
        s5.executeUpdate();
        s5.close();
      }
      rs3.close();
      s3.close();

      c.commit();
    }
    catch (SQLException e){
      try{
        c.rollback();
      }
      catch (Exception e2){}
    }
    finally {
      try {
        c.close();
      }
      catch (Exception e){}
    }
  }
}