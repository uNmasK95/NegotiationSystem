package data;

import exception.AcoesInsuficientesException;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.*;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;

/**
 * Created by pedro on 23-12-2016.
 */
public class Transacoes {
    public Transacoes(){}

    public void deliveryVsPayment(String from, String to, String empresa, float preco, int quantidade)
        throws AcoesInsuficientesException, RollbackException {
      Context ctx = null;
      UserTransaction txn = null;
      DataSource ds1 = null;
      try {
        ctx = new InitialContext();

        txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");

        txn.begin();
        ds1 = (DataSource) ctx.lookup("jdbc/acoes");

        Connection c1 = ds1.getConnection();

        // Verificar se utilizador "from" tem acoes suficientes para vender
        PreparedStatement s1 = c1.prepareStatement("" +
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
            txn.rollback();
          }
          catch (Exception e){}
          throw new AcoesInsuficientesException();
        }
        rs1.close();
        s1.close();

        // Retirar acoes ao utilizador "from"
        PreparedStatement s2 = c1.prepareStatement("" +
            "update acoes set quantidade = quantidade - ? " +
            "where utilizador = ? and empresa = ?");
        s2.setInt(1, quantidade);
        s2.setString(2, from);
        s2.setString(3, empresa);
        s2.executeUpdate();
        s2.close();
        // Se quantidade ficar a 0, eliminar registo
        if(qtdAntiga == quantidade){
          PreparedStatement s99 = c1.prepareStatement("" +
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
        PreparedStatement s3 = c1.prepareStatement("" +
            "select quantidade from acoes " +
                "where utilizador = ? and empresa = ?"
        );
        s3.setString(1,to);
        s3.setString(2,empresa);
        ResultSet rs3 = s3.executeQuery();
        if(rs3.next()){
          // Ja existe um registo
          // Atualizar quantidade
          PreparedStatement s4 = c1.prepareStatement("" +
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
          PreparedStatement s5 = c1.prepareStatement("" +
              "insert into acoes values " +
              "(?,?,?)");
          s5.setString(1,empresa);
          s5.setString(2,to);
          s5.setInt(3,quantidade);
          s5.executeUpdate();
          s5.close();
        }
        c1.close();

        txn.commit();
      }
      catch(SQLException e){
        e.printStackTrace();
        try {
          txn.rollback();
        } catch (SystemException e1) {
          e1.printStackTrace();
        }
      }
      catch (RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
        throw new RollbackException(e.getMessage());
      } catch (SystemException e) {
        e.printStackTrace();
      } catch (NamingException e) {
        e.printStackTrace();
      } catch (NotSupportedException e) {
        e.printStackTrace();
      }

      // TODO - TRANSFERENCIAS NO BANCO
//      System.out.println("A dormir...");
//      try {
//        System.in.read();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//      System.out.println("A continuar!");
//
//      ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/books");
//      javax.jms.Connection c2 = cf.createConnection();
//      Session s = c2.createSession(false, 0);
//      Destination q = s.createQueue("FILA1");
//      MessageProducer p = s.createProducer(q);
//
//      TextMessage m = s.createTextMessage("teste!");
//      p.send(m);
//
//      p.close();
//      s.close();
//      c2.close();

    }
}
