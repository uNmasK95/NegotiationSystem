import data.Banco;
import exception.InsuficientFundsException;
import exception.UserNotFoundException;

/**
 * Created by pedro on 01-01-2017.
 */
public class Transferencia {
  public static void main(String[] args) {
    Banco b = new Banco();
    try {
      b.transfer("freitas","pedro",100.00f);
    } catch (InsuficientFundsException e) {
      e.printStackTrace();
    } catch (UserNotFoundException e) {
      e.printStackTrace();
    }
  }
}
