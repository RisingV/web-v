package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-18  <br/>
 */
public class ScanException extends Exception implements Expected {

    ScanException() {}

    public ScanException( String message ) {
        super( message );
    }

    public ScanException( String message, Throwable err ) {
        super( message, err );
    }

}
