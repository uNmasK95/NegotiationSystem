import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.Statement;

public class Buy1Topic {
    public static void main(String[] args) throws Exception {

        Context ctx = new InitialContext();

        UserTransaction txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");

        txn.begin();

        DataSource ds1 = (DataSource) ctx.lookup("jdbc/books");
        Connection c1 = ds1.getConnection();
        Statement s1 = c1.createStatement();
        s1.executeUpdate("update books set stock = stock - 1 where isbn = 2");
        s1.close();
        c1.close();

        System.out.println("A dormir...");
        System.in.read();
        System.out.println("A continuar!");

        ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/books");
        javax.jms.Connection c2 = cf.createConnection();
        Session s = c2.createSession(false, 0);
        Destination q = s.createTopic("FILA1");
        MessageProducer p = s.createProducer(q);

        TextMessage m = s.createTextMessage("teste!");
        m.setIntProperty("price", 10);
        p.send(m);

        p.close();
        s.close();
        c2.close();

        txn.commit();

        System.out.println("Done!");
    }
}
