package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-19  <br/>
 */
public class RegisterException extends RuntimeException implements Expected {

    public RegisterException() {
    }

    public RegisterException( Throwable cause ) {
        super( cause );
    }

    public RegisterException( String message ) {
        super( message );
    }

    public RegisterException( String message, Throwable cause ) {
        super( message, cause );
    }

}
