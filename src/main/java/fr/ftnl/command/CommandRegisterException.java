package fr.ftnl.command;

public class CommandRegisterException extends RuntimeException {
  
  public CommandRegisterException() {
    super();
  }

  public CommandRegisterException(String message) {
    super(message);
  }

  public CommandRegisterException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandRegisterException(Throwable cause) {
    super(cause);
  }
}