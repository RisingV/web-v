package vee.web.support;

import vee.web.servlet.RequestContext;

import java.util.function.Function;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-17  <br/>
 */
public class RequestContextHelper {

    private final RequestContext requestContext;

    public RequestContextHelper( RequestContext requestContext ) {
        this.requestContext = requestContext;
    }

    private <T> T getParam( String key, Function<String, T> f ) {
        if ( null != requestContext ) {
            try {
                return f.apply( requestContext.getParam( key ) );
            } catch ( Exception ignored ) {
            }
        }
        return null;
    }

    public String getParamString( String key ) {
        return getParam( key, s -> s );
    }

    public Integer getParamInt( String key ) {
        return getParam( key, Integer::valueOf );
    }

    public Long getParamLong( String key ) {
        return getParam( key, Long::valueOf );
    }

    public Float getParamFloat( String key ) {
        return getParam( key, Float::valueOf );
    }

    public Double getParamDouble( String key ) {
        return getParam( key, Double::valueOf );
    }

    //TODO

}
