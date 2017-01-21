package main;

import data.BancoConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;


public class Banco {

  private static BancoConnection bc = new BancoConnection();
  private static Connection connection;
  private static Session session;
  private static MessageConsumer consumer;

  public static void main(String[] args) throws JMSException {
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
      Destination q = session.createQueue("transferencias");
      consumer = session.createConsumer(q);

      start();
    }

  private static void start() throws JMSException {
    TextMessage message;
    String from, to;
    float amount;
    while(true){
      message = (TextMessage) consumer.receive();
      if(message.getText().equals("transferencia")){
       from = message.getStringProperty("emissor");
       to = message.getStringProperty("recetor");
       amount = message.getFloatProperty("quantidade");
       bc.transfer(from,to,amount);
       System.out.println("Transferencia: "+from+" -> "+to+" ("+amount+")");
      }
    }
  }
}
