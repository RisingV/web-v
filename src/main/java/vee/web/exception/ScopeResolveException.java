package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public class ScopeResolveException extends IllegalStateException implements Expected {

    private static final long serialVersionUID = 1307903603850091320L;

    public ScopeResolveException() {
    }

    public ScopeResolveException( Throwable cause ) {
        super( cause );
    }

    public ScopeResolveException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ScopeResolveException( String s ) {
        super( s );
    }

}
