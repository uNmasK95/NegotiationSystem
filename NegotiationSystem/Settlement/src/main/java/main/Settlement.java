package main;

import exception.AcoesInsuficientesException;
import exception.UserNotFoundException;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.*;
import java.sql.*;
import java.sql.Connection;

public class Settlement {
  public static void main(String[] args) throws Exception {
    new Settlement().start();
  }

  private Context ctx;
  private UserTransaction txn;
  private javax.jms.Connection connection;
  private Connection c_acoes;
  private Session session_exchange;
  private MessageConsumer mc;
  private MessageProducer replier;

  private AcoesListener acoesListener;
  private Session session_acoes;
  private Connection connection_acoes;

  private Session session_banco;
  private MessageProducer bancoProducer;

  private Settlement() throws NamingException, JMSException, SQLException {
    ctx = new InitialContext();

    txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/amq");

    connection = cf.createConnection();
    connection.start();

    DataSource ds_acoes = (DataSource) ctx.lookup("jdbc/acoes");
    c_acoes = ds_acoes.getConnection();

    //Comunicacao com o banco
    session_banco = connection.createSession(false,0);
    Destination q_banco = session_banco.createQueue("transferencias");
    bancoProducer = session_banco.createProducer(q_banco);

    //Comunicacao com exchange
    session_exchange = connection.createSession(false,0);
    Destination q = session_exchange.createQueue("vendas");
    mc = session_exchange.createConsumer(q);
    replier = session_exchange.createProducer(null);

    //Iniciar acoesListener
    session_acoes = connection.createSession(false, 0);
    connection_acoes = ds_acoes.getConnection();
    acoesListener = new AcoesListener(connection_acoes, session_acoes);
  }

  private void start() throws NamingException, JMSException, SQLException, SystemException, NotSupportedException {
    while(true){
      boolean ok = true;
      Exception erro = null;

      System.out.println("Settlement > receiving...");
      TextMessage request = (TextMessage) mc.receive();
      System.out.print("Settlement > received \""+request.getText()+"\"... ");

      if(request.getText().equals("venda")) {
        txn.begin();
        try{
          deliveryVSpayment(
              request.getStringProperty("comprador"),
              request.getStringProperty("vendedor"),
              request.getStringProperty("empresa"),
              request.getIntProperty("quantidade"),
              request.getFloatProperty("preco"),
              c_acoes);
          System.out.println("OK");
        } catch (UserNotFoundException
            | AcoesInsuficientesException
            | SQLException e) {
          e.printStackTrace();
          ok = false;
          erro = e;
        }
      }
      else ok = false;

      TextMessage response = session_exchange.createTextMessage();

      if(ok){
        try{
          txn.commit();
          try{
            response.setText("OK");
            response.setJMSCorrelationID(request.getJMSCorrelationID());
            replier.send(request.getJMSReplyTo(), response);
          }
          catch (JMSException e){
            e.printStackTrace();
          }
        }
        catch (RollbackException | HeuristicRollbackException | HeuristicMixedException e){
          e.printStackTrace();
        }
      }
      else{
        try {
          switch (erro.getClass().getSimpleName()) {
            case "AcoesInsuficientesException":
              response.setStringProperty("erro", "acoes");
              response.setIntProperty("acoes", Integer.parseInt(erro.getMessage()));
              break;
            case "UserNotFoundException":
              response.setStringProperty("erro", "utilizador");
              response.setStringProperty("utilizador", erro.getMessage());
              break;
            default:
              response.setStringProperty("erro", "erro");
          }
          try{
            response.setText("KO");
            response.setJMSCorrelationID(request.getJMSCorrelationID());
            replier.send(request.getJMSReplyTo(), response);
          }
          catch (JMSException e){
            e.printStackTrace();
          }
          txn.rollback();
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    }
  }

  private void deliveryVSpayment(
          String comprador, String vendedor, String empresa, int quantidade, float preco,
          Connection acoes)
      throws AcoesInsuficientesException, SQLException, UserNotFoundException, JMSException {
    transferirAcoes(vendedor,comprador,empresa,quantidade,acoes);

    transferirDinheiro(comprador,vendedor,preco);
  }

  private void transferirDinheiro(String comprador, String vendedor, float preco) throws JMSException {
    TextMessage m = session_banco.createTextMessage("transferencia");
    m.setStringProperty("emissor",comprador);
    m.setStringProperty("recetor",vendedor);
    m.setFloatProperty("quantidade",preco);

    bancoProducer.send(m);
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
