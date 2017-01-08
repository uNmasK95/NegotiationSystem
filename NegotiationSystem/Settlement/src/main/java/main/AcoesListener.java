package main;

import data.Acoes;

import javax.jms.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class AcoesListener implements MessageListener {

  private Connection connection;
  private Destination queue;
  private MessageConsumer consumer;
  private MessageProducer replier;
  private Session session;
  private Acoes acoes;

  public AcoesListener(Connection connection, Session session) throws JMSException {
    this.connection = connection;
    this.session = session;
    this.queue = session.createQueue("acoes");
    this.consumer = session.createConsumer(queue);
    this.replier = session.createProducer(null);
    this.acoes = new Acoes(connection);
    consumer.setMessageListener(this);
  }

  @Override
  public void onMessage(Message message) {
    if (message instanceof TextMessage) {
      try {
        TextMessage request = (TextMessage) message;
        System.out.println("AcoesListener > received "+request.getText());
        TextMessage reply = session.createTextMessage();
        if(request.getText().toUpperCase().equals("GET")){
          if(request.propertyExists("utilizador")){
            //Responder com acoes do utilizador
            String utilizador = request.getStringProperty("utilizador");
            try {
              Map<String, Integer> empresas = acoes.getAcoesUtilizador(utilizador);
              reply.setText("utilizador");
              for(String empresa: empresas.keySet()) {
                reply.setIntProperty(empresa, empresas.get(empresa));
              }
              reply.setJMSCorrelationID(request.getJMSCorrelationID());
              replier.send(request.getJMSReplyTo(), reply);
            }
            catch (SQLException e){
              e.printStackTrace();
              this.error(request);
            }
            catch (JMSException e){
              e.printStackTrace();
            }
          }
          else {
            //Responder com todas as acoes
            reply.setText("empresas");
            try {
              for (String empresa : acoes.getEmpresas()) {
                reply.setBooleanProperty(empresa, true);
              }
              reply.setJMSCorrelationID(request.getJMSCorrelationID());
              replier.send(request.getJMSReplyTo(), reply);
            }catch (SQLException e){
              e.printStackTrace();
              this.error(request);
            }
            catch (JMSException e){
              e.printStackTrace();
            }
          }
        }
        else{
          // Ignorar...
        }
      } catch (JMSException e) {
        e.printStackTrace();
      }
    }
  }

  private void error(TextMessage request) {
    try {
      TextMessage reply = session.createTextMessage();
      reply.setText("erro");
      reply.setJMSCorrelationID(request.getJMSCorrelationID());
      replier.send(request.getJMSReplyTo(), reply);
    }catch (JMSException e){
      e.printStackTrace();
    }
  }


}
