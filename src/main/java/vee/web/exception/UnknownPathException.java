package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public class UnknownPathException extends RuntimeException implements Expected {

    public UnknownPathException() {
    }

    public UnknownPathException( String message ) {
        super( message );
    }

    public UnknownPathException( String message, Throwable cause ) {
        super( message, cause );
    }

}
