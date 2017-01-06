package main;

import data.Banco;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;

public class Server {
  public static void main(String[] args) throws NamingException, JMSException, SystemException, NotSupportedException {
    Context ctx = new InitialContext();
    Banco banco = new Banco();

    UserTransaction txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    //txn.setTransactionTimeout(Integer.MAX_VALUE);

    ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/settlement");
    javax.jms.Connection c1 = cf.createConnection();
    c1.start();

    Session s = null;
    Queue q;
    MessageConsumer mc = null;

    System.out.println("Creating consumer...");
    s = c1.createSession(false, 0);
    q = s.createQueue("Transferencias");
    mc = s.createConsumer(q);

    while(true) {
      try {
        System.out.println("receiving...");
        TextMessage m = (TextMessage) mc.receive();
        if(m.getText().equals("transferencia")) {
          banco.transfer(
              m.getStringProperty("emissor"),
              m.getStringProperty("recetor"),
              m.getFloatProperty("montante"));
        }
        System.out.println("recebi: " + m.getText());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
