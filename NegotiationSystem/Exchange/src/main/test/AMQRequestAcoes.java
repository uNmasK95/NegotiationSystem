import javax.jms.*;
import javax.jms.Connection;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Enumeration;

public class AMQRequestAcoes {

  public static void main(String[] args) throws JMSException, InterruptedException {
    ActorRef<Void> testActor = new TestActor2().spawn();
  }

}

class TestActor2 extends BasicActor<Void,Void> implements MessageListener {
  private Connection connection;

  public TestActor2() throws JMSException {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
    this.connection = connectionFactory.createConnection();
  }

  @Override
  protected Void doRun() throws InterruptedException, SuspendExecution {
    try {
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination q = session.createQueue("acoes");

      MessageProducer producer = session.createProducer(q);

      Destination tempDest = session.createTemporaryQueue();
      MessageConsumer responseConsumer = session.createConsumer(tempDest);

      responseConsumer.setMessageListener(this);

      TextMessage m = session.createTextMessage();
      m.setText("get");
      //m.setStringProperty("utilizador","freitas");

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
        TextMessage reply = (TextMessage) message;
        messageText = reply.getText();
        System.out.println("messageText = " + messageText);
        Enumeration enumeration = reply.getPropertyNames();
        while(enumeration.hasMoreElements()){
          String empresa = (String)enumeration.nextElement();
          switch (messageText) {
            case "utilizador":
              System.out.println(empresa + " - " + reply.getIntProperty(empresa));
              break;
            case "empresas":
              System.out.println(empresa);
              break;
          }
        }
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
