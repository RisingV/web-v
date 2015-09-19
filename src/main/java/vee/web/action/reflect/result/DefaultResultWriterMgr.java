package vee.web.action.reflect.result;


import vee.web.exception.RegisterException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public class DefaultResultWriterMgr implements ResultWriterMgr {

    private final static class SingleDefaultResultWriterMgr {
        private final static ResultWriterMgr shared = new DefaultResultWriterMgr();
    }

    public static ResultWriterMgr sharedInstance() {
        return SingleDefaultResultWriterMgr.shared;
    }

    public static ResultWriterMgr newInstance() {
        return new DefaultResultWriterMgr();
    }

    private DefaultResultWriterMgr() {
    }

    private final Map<Class<?>, ResultWriter> resultWriters = new HashMap<>();

    private ResultWriter defaultWriter;

    @Override
    public void registerResultWriter( Class<?> resultType, ResultWriter resultWriter ) {
        synchronized ( resultWriters ) {
            if ( resultWriters.containsKey( resultType ) ) {
                throw new RegisterException( " result writer for type '" +
                        ( ( null != resultType ) ? resultType.getName() : "null" ) + "' already registered!" );
            }
            if ( null != resultType && null != resultWriter ) {
                resultWriters.put( resultType, resultWriter );
            }
        }
    }

    @Override
    public void setDefault( ResultWriter resultWriter ) {
        this.defaultWriter = resultWriter;
    }

    @Override
    public ResultWriter getResultWriter( Class<?> resultType ) {
        ResultWriter resultWriter = getLocal( resultType );
        if ( null == resultWriter ) {
            resultWriter = ( (DefaultResultWriterMgr) sharedInstance() ).getLocal( resultType );
        }
        return null != resultWriter ? resultWriter : getDefaultWriter();
    }

    private ResultWriter getDefaultWriter() {
        return null != defaultWriter ? defaultWriter : ( (DefaultResultWriterMgr) sharedInstance() ).defaultWriter;
    }

    private ResultWriter getLocal( Class<?> resultType ) {
        ResultWriter resultWriter = resultWriters.get( resultType );
        if ( null == resultWriter && null != resultType ) {
            resultWriter = getByTypeMatching( resultType );
        }
        return resultWriter;
    }

    private ResultWriter getByTypeMatching( Class<?> resultType ) {
        synchronized ( resultWriters ) {
            for ( Map.Entry<Class<?>, ResultWriter> entry : resultWriters.entrySet() ) {
                if ( entry.getKey().isAssignableFrom( resultType ) ) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

}
