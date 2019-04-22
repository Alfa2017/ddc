package ddc.exception;

/**
 * Исключения, возникающие в FileHandler-ах
 */
public class ServerErrorDdsException extends Exception {

    public ServerErrorDdsException(String message) {
        super(message);
    }

    public ServerErrorDdsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerErrorDdsException(Throwable cause){
        super(cause.getMessage(), cause);
    }

    public void setMessage(String message) {
        setMessage(message);
    }
}
