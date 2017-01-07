package exception;

/**
 * Created by pedro on 23-12-2016.
 */
public class BancoIndisponivelException extends Exception {
  public BancoIndisponivelException(){
    super();
  }

  public BancoIndisponivelException(String msg){
    super(msg);
  }
}
