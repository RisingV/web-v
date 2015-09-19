package vee.web.exception;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: francis Yun    <br/>
 * Date: 2013-11-29  <br/>
 */
public class ParamLostException extends IllegalStateException implements Expected {

    private static final long serialVersionUID = 8237726872596653585L;

    public ParamLostException() {
        super();
    }

    public ParamLostException( String message ) {
        super( message );
    }

    public ParamLostException( String message, Throwable cause ) {
        super( message, cause );
    }

}
