package exception;

/**
 * Created by pedro on 23-12-2016.
 */
public class UserNotFoundException extends Exception {
  public UserNotFoundException(){
    super();
  }

  public UserNotFoundException(String msg){
    super(msg);
  }
}
