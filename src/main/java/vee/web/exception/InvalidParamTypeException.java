package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: francis Yun    <br/>
 * Date: 2013-11-30  <br/>
 */
public class InvalidParamTypeException extends IllegalStateException implements Expected {

    private static final long serialVersionUID = 6413408014180918416L;

    public InvalidParamTypeException() {
        super();
    }

    public InvalidParamTypeException( String message ) {
        super( message );
    }

    public InvalidParamTypeException( String message, Throwable cause ) {
        super( message, cause );
    }

}
