package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public class ParamResolveException extends IllegalStateException implements Expected {

    private static final long serialVersionUID = 2291728585067441759L;

    public ParamResolveException() {
    }

    public ParamResolveException( String message ) {
        super( message );
    }

    public ParamResolveException( String message, Throwable cause ) {
        super( message, cause );
    }

}
