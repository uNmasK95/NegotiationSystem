package exception;

/**
 * Created by pedro on 23-12-2016.
 */
public class InsuficientFundsException extends Exception {
  public InsuficientFundsException(){
    super();
  }

  public InsuficientFundsException(String msg){
    super(msg);
  }
}
