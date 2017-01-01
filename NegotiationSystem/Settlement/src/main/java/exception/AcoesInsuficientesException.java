package exception;

/**
 * Created by pedro on 23-12-2016.
 */
public class AcoesInsuficientesException extends Exception {
  public AcoesInsuficientesException(){
    super();
  }

  public AcoesInsuficientesException(String msg){
    super(msg);
  }
}
