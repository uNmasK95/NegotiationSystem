import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;

/**
 * Created by pedro on 01-01-2017.
 */
public class Transferencia {
  public static void main(String[] args) throws Exception{
    String vendedor = "pedro";
    String comprador = "freitas";
    float preco = 300.0f;
    String empresa = "microsoft";
    int quantidade = 5;

    ConnectionFactory cf = null;
    Context ctx = new InitialContext();

    UserTransaction txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    txn.begin();

    cf = (ConnectionFactory) ctx.lookup("jms/amq");
    javax.jms.Connection cb = cf.createConnection();
    cb.start();

    Session s = cb.createSession(false, 0);
    Queue q = s.createQueue("vendas");
    MessageProducer p = s.createProducer(q);

    TextMessage m = s.createTextMessage("venda");
    m.setStringProperty("comprador",comprador);
    m.setStringProperty("vendedor",vendedor);
    m.setStringProperty("empresa",empresa);
    m.setIntProperty("quantidade",quantidade);
    m.setFloatProperty("preco",preco);
    p.send(m);

    p.close();
    s.close();
    cb.close();

    txn.commit();
  }
}
