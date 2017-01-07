import javax.jms.*;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import org.apache.activemq.ActiveMQConnectionFactory;

public class AMQRequest {

  public static void main(String[] args) throws JMSException, InterruptedException {
    ActorRef<Void> testActor = new TestActor().spawn();
  }

}

class TestActor extends BasicActor<Void,Void> implements MessageListener {
  private Connection connection;

  public TestActor() throws JMSException {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
    this.connection = connectionFactory.createConnection();
  }

  @Override
  protected Void doRun() throws InterruptedException, SuspendExecution {
    try {
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination q = session.createQueue("vendas");

      MessageProducer producer = session.createProducer(q);

      Destination tempDest = session.createTemporaryQueue();
      MessageConsumer responseConsumer = session.createConsumer(tempDest);

      responseConsumer.setMessageListener(this);

      TextMessage m = session.createTextMessage("venda");
      m.setStringProperty("comprador","freitas");
      m.setStringProperty("vendedor","pedro");
      m.setStringProperty("empresa","microsoft");
      m.setIntProperty("quantidade",1);
      m.setFloatProperty("preco",2.0f);

      // Testes de excecoes ...
      //m.setStringProperty("comprador","UNKOWN");
      //m.setStringProperty("vendedor","UNKOWN");
      //m.setIntProperty("quantidade",100);
      //m.setFloatProperty("preco",200000.0f);

      m.setJMSReplyTo(tempDest);

      //String correlationId = this.createRandomString();
      //m.setJMSCorrelationID("0");
      // FIXME Sera necessario criar ids?

      producer.send(m);
    }
    catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void onMessage(Message message) {
    String messageText = null;
    try {
      if (message instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) message;
        messageText = textMessage.getText();
        if(messageText.equals("OK"))
          System.out.println("OK");
        else if(messageText.equals("KO")){
          System.err.print("ERRO: ");
          String erro = textMessage.getStringProperty("erro");
          switch (erro){
            case "dinheiro":
              float saldo = textMessage.getFloatProperty("saldo");
              System.err.println("Saldo insuficiente - " + saldo); //saldo atual
              break;
            case "acoes":
              int acoes = textMessage.getIntProperty("acoes");
              System.err.println("Acoes insuficentes - "+acoes);
              break;
            case "utilizador":
              String utilizador = textMessage.getStringProperty("utilizador");
              System.err.println("Utilizador nao existente - "+utilizador);
              break;
            default:
              System.err.println("???");
          }
        }
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
