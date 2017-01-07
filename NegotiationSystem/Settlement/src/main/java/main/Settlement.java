package main;

import exception.AcoesInsuficientesException;
import exception.BancoIndisponivelException;
import exception.InsuficientFundsException;
import exception.UserNotFoundException;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.*;
import java.sql.*;
import java.sql.Connection;

// TODO - Tratar indisponibilidade do banco
public class Settlement {
  public static void main(String[] args) throws Exception {
    new Settlement().start();
  }

  private Context ctx;
  private UserTransaction txn;
  private javax.jms.Connection connection;
  private Connection c_acoes;
  private Connection c_banco;
  private Session session;
  private MessageConsumer mc;
  private MessageProducer replier;
  private AcoesListener acoesListener;
  private Session session_acoes;
  private Connection connection_acoes;


  private Settlement() throws NamingException, JMSException, SQLException {
    ctx = new InitialContext();
    txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/amq");
    connection = cf.createConnection();

    DataSource ds_acoes = (DataSource) ctx.lookup("jdbc/acoes");
    c_acoes = ds_acoes.getConnection();

    DataSource ds_banco = (DataSource) ctx.lookup("jdbc/banco");
    c_banco = ds_banco.getConnection();

    session = connection.createSession(false,0);
    Destination q = session.createQueue("vendas");
    mc = session.createConsumer(q);
    replier = session.createProducer(null);

    //Iniciar acoesListener
    session_acoes = connection.createSession(false,0);
    connection_acoes = ds_acoes.getConnection();
  }

  private void start() throws NamingException, JMSException, SQLException, SystemException, NotSupportedException {
    connection.start();
    acoesListener = new AcoesListener(connection_acoes,session_acoes);

    while(true){
      txn.setTransactionTimeout(Integer.MAX_VALUE);
      txn.begin();

      boolean ok = true;
      Exception erro = null;

      System.out.println("Settlement > receiving...");
      TextMessage request = (TextMessage) mc.receive();
      System.out.print("Settlement > received \""+request.getText()+"\"... ");

      if(request.getText().equals("venda")) {
        try{
          deliveryVSpayment(
              request.getStringProperty("comprador"),
              request.getStringProperty("vendedor"),
              request.getStringProperty("empresa"),
              request.getIntProperty("quantidade"),
              request.getFloatProperty("preco"),
              c_acoes,
              c_banco);
          System.out.println("OK");
        } catch (InsuficientFundsException
            | UserNotFoundException
            | AcoesInsuficientesException
            | BancoIndisponivelException
            | SQLException e) {
          e.printStackTrace();
          ok = false;
          erro = e;
        }
      }
      else ok = false;

      TextMessage response = session.createTextMessage();

      if(ok){
        try{
          response.setText("OK");
          response.setJMSCorrelationID(request.getJMSCorrelationID());
          replier.send(request.getJMSReplyTo(), response);
          txn.commit();
        }
        catch (RollbackException | HeuristicRollbackException | HeuristicMixedException e){
          e.printStackTrace();
        }
      }
      else{
        switch (erro.getClass().getSimpleName()){
          case "InsuficientFundsException":
            response.setStringProperty("erro","dinheiro");
            response.setFloatProperty("saldo", Float.parseFloat(erro.getMessage()));
            txn.rollback();
            break;
          case "AcoesInsuficientesException":
            response.setStringProperty("erro","acoes");
            response.setIntProperty("acoes",Integer.parseInt(erro.getMessage()));
            txn.rollback();
            break;
          case "UserNotFoundException":
            response.setStringProperty("erro","utilizador");
            response.setStringProperty("utilizador",erro.getMessage());
            txn.rollback();
            break;
          case "BancoIndisponivelException":
            // TODO - Tratar este caso ...
            response.setStringProperty("erro","erro");
            txn.rollback();
            break;
          default:
            response.setStringProperty("erro","erro");
            txn.rollback();
        }
        response.setText("KO");
        response.setJMSCorrelationID(request.getJMSCorrelationID());
        replier.send(request.getJMSReplyTo(), response);
      }
    }
  }

  private void deliveryVSpayment(
          String comprador, String vendedor, String empresa, int quantidade, float preco,
          Connection acoes, Connection banco)
      throws AcoesInsuficientesException, SQLException, UserNotFoundException, InsuficientFundsException, BancoIndisponivelException
  {
    transferirAcoes(vendedor,comprador,empresa,quantidade,acoes);
    transferirDinheiro(comprador,vendedor,preco,banco);
  }

  private void transferirDinheiro(String from, String to, float amount, Connection c) throws InsuficientFundsException, UserNotFoundException, BancoIndisponivelException {

    try {
      //Verificar se utilizador "from" tem saldo suficiente
      PreparedStatement s1 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?");
      s1.setString(1, from);
      ResultSet rs1 = s1.executeQuery();
      float saldo_from = 0;
      if (rs1.next()) {
        saldo_from = rs1.getFloat("saldo");
        if (saldo_from < amount) {
          try {
            rs1.close();
            s1.close();
          } catch (Exception e) {
          }
          throw new InsuficientFundsException("" + saldo_from);
        }
      }
      // utilizador "from" nao existe
      else {
        try {
          rs1.close();
          s1.close();
        } catch (Exception e) {
        }
        throw new UserNotFoundException("" + from);
      }

      //Retirar saldo a "from"
      PreparedStatement s2 = c.prepareStatement("" +
          "update utilizadores set saldo = saldo - ? " +
          "where username = ?");
      s2.setFloat(1, amount);
      s2.setString(2, from);
      s2.executeUpdate();
      s2.close();

      //Creditar saldo no utilizador "to"
      // Verificar se utilizador "to" existe
      PreparedStatement s3 = c.prepareStatement("" +
          "select saldo from utilizadores " +
          "where username = ?"
      );
      s3.setString(1, to);
      ResultSet rs3 = s3.executeQuery();
      if (rs3.next()) {
        // Utilizador "to" existe
        PreparedStatement s4 = c.prepareStatement("" +
            "update utilizadores set saldo = saldo + ? " +
            "where username = ?"
        );
        s4.setFloat(1, amount);
        s4.setString(2, to);
        s4.executeUpdate();
        s4.close();
      } else {
        // utilizador "to" nao existe
        try {
          rs3.close();
          s3.close();
        } catch (Exception e) {
        }
        throw new UserNotFoundException("" + to);
      }
      rs3.close();
      s3.close();
    }
    catch (SQLException e){
      throw new BancoIndisponivelException();
    }
  }

  public void transferirAcoes(String from, String to, String empresa, int quantidade, Connection c)
      throws AcoesInsuficientesException, SQLException {

      // Verificar se utilizador "from" tem acoes suficientes para vender
      PreparedStatement s1 = c.prepareStatement("" +
          "select quantidade from acoes " +
          "where utilizador = ? and empresa = ?");
      s1.setString(1, from);
      s1.setString(2, empresa);
      ResultSet rs1 = s1.executeQuery();
      int qtdAntiga = 0;
      if (!rs1.next() || (qtdAntiga=rs1.getInt("quantidade")) < quantidade){
        try{
          rs1.close();
          s1.close();
        }
        catch (Exception e){}
        throw new AcoesInsuficientesException(""+qtdAntiga);
      }
      rs1.close();
      s1.close();

      // Retirar acoes ao utilizador "from"
      PreparedStatement s2 = c.prepareStatement("" +
          "update acoes set quantidade = quantidade - ? " +
          "where utilizador = ? and empresa = ?");
      s2.setInt(1, quantidade);
      s2.setString(2, from);
      s2.setString(3, empresa);
      s2.executeUpdate();
      s2.close();
      // Se quantidade ficar a 0, eliminar registo
      if(qtdAntiga == quantidade){
        PreparedStatement s99 = c.prepareStatement("" +
            "delete from acoes where " +
            "utilizador = ? and empresa = ?");
        s99.setString(1,from);
        s99.setString(2,empresa);
        s99.executeUpdate();
        s99.close();
      }

      // Adicionar acoes ao utilizador "to"
      // Verificar se ja existe um registo relativo
      // ao utilizador "to" e a empresa "empresa"
      PreparedStatement s3 = c.prepareStatement("" +
          "select quantidade from acoes " +
          "where utilizador = ? and empresa = ?"
      );
      s3.setString(1,to);
      s3.setString(2,empresa);
      ResultSet rs3 = s3.executeQuery();
      if(rs3.next()){
        // Ja existe um registo
        // Atualizar quantidade
        PreparedStatement s4 = c.prepareStatement("" +
            "update acoes set quantidade = quantidade + ? " +
            "where utilizador = ? and empresa = ?"
        );
        s4.setInt(1, quantidade);
        s4.setString(2, to);
        s4.setString(3, empresa);
        s4.executeUpdate();
        s4.close();
      }
      else{
        // Novo registo
        PreparedStatement s5 = c.prepareStatement("" +
            "insert into acoes values " +
            "(?,?,?)");
        s5.setString(1,empresa);
        s5.setString(2,to);
        s5.setInt(3,quantidade);
        s5.executeUpdate();
        s5.close();
      }
  }
}
