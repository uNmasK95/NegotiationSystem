import data.Acoes;
import data.Transacoes;
import exception.AcoesInsuficientesException;
import exception.UserNotFoundException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.transaction.*;
import java.sql.SQLException;

/**
 * Created by pedro on 23-12-2016.
 */
public class Test {
  public static void main(String[] args) throws SQLException, UserNotFoundException {

    Transacoes t = new Transacoes();
//    try {
//      t.deliveryVsPayment("pedro","freitas","samsung", 10,1);
//    }
//    catch (AcoesInsuficientesException e) {
//      e.printStackTrace();
//    }
//    catch (Exception e){
//      e.printStackTrace();
//    }
    try {
      t.bankTransfer("pedro","freitas",10);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
