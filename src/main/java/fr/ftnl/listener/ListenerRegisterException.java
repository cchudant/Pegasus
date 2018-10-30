package fr.ftnl.listener;

public class ListenerRegisterException extends RuntimeException {
  
  public ListenerRegisterException() {
    super();
  }

  public ListenerRegisterException(String message) {
    super(message);
  }

  public ListenerRegisterException(String message, Throwable cause) {
    super(message, cause);
  }

  public ListenerRegisterException(Throwable cause) {
    super(cause);
  }
}