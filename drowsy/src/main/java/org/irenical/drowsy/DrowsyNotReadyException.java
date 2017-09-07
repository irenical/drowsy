package org.irenical.drowsy;

public class DrowsyNotReadyException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public DrowsyNotReadyException(Throwable cause) {
    super("Drowsy instance is not ready yet", cause);
  }

}
