import data.Acoes;
import data.Banco;
import exception.UserNotFoundException;

import java.sql.SQLException;

/**
 * Created by pedro on 23-12-2016.
 */
public class Test {
  public static void main(String[] args) throws SQLException, UserNotFoundException {
    Banco b = new Banco();
    float saldoP = b.getSaldo("pedro");
    System.out.println(saldoP);
    float notFound = b.getSaldo("notfound");
    System.out.println(notFound);

    Acoes a = new Acoes();
    int acoesP = a.getAcoes("microsoft","pedro");
    System.out.println(acoesP);
    int notFound2 = a.getAcoes("notfound","notfound");
    System.out.println(notFound2);
  }
}
