import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.Statement;

public class Buy2 {
    public static void main(String[] args) throws Exception {

        Context ctx = new InitialContext();

        UserTransaction txn = (UserTransaction) ctx.lookup("java:comp/UserTransaction");

        txn.begin();

        ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/banco");
        javax.jms.Connection c1 = cf.createConnection();
        c1.start();


        Session s = c1.createSession(false, 0);
        Destination q = s.createQueue("FILA1");
        MessageConsumer mc = s.createConsumer(q);

        System.out.println("receiving...");
        TextMessage m = (TextMessage) mc.receive();
        System.out.println("recebi: "+m.getText());

        mc.close();
        s.close();
        c1.close();

        System.out.println("A dormir...");
        System.in.read();
        System.out.println("A continuar!");

        DataSource ds2 = (DataSource) ctx.lookup("jdbc/books2");
        Connection c2 = ds2.getConnection();
        Statement s2 = c2.createStatement();
        s2.executeUpdate("update books set stock = stock - 1 where isbn = 6");
        s2.close();
        c2.close();

        txn.commit();

        System.out.println("Done!");

    }
}
