package vee.web.action.reflect.param;

import vee.web.action.tag.scope.*;
import vee.web.exception.ScopeResolveException;
import vee.web.servlet.RequestContext;
import vee.web.util.Util;
import vee.web.wild.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public enum ParamScope {

    REQUEST_PARAM,
    REQUEST_BODY,
    HEADER,
    SESSION,
    CONTEXT,
    COOKIE,
    ATTRIBUTE;

    private static final ParamScope DEFAULT_SCOPE = REQUEST_PARAM;

    private static class Resolvers<T extends Annotation> {

        private static final Resolvers instance = new Resolvers<>();

        private final Map<Class<T>, BiFunction<T, BiFunction<T, String, Void>, ParamScope>> resolvers = new LinkedHashMap<>();

        private void registerResolvers( Class<T> clazz, BiFunction<T, BiFunction<T, String, Void>, ParamScope> resolver ) {
            resolvers.put( clazz, resolver );
        }

        private BiFunction<T, BiFunction<T, String, Void>, ParamScope> getResolver( Class<T> clazz ) {
            return resolvers.get( clazz );
        }

    }

    @SuppressWarnings( "unchecked" )
    private static <T extends Annotation> void registerResolvers( Class<T> clazz, BiFunction<T, BiFunction<T, String, Void>, ParamScope> resolver ) {
        Resolvers.instance.registerResolvers( clazz, resolver );
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends Annotation> BiFunction<T, BiFunction<T, String, Void>, ParamScope> getResolver( Class<T> clazz ) {
        return Resolvers.instance.getResolver( clazz );
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends Annotation> ParamScope resolve( T t, BiFunction<T, String, Void> paramNameCallback ) {
        BiFunction<T, BiFunction<T, String, Void>, ParamScope> f = getResolver( (Class<T>) t.annotationType() );
        return null != f ? f.apply( t, paramNameCallback ) : null;
    }

    static {
        registerResolvers( Param.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return REQUEST_PARAM;
        } );
        registerResolvers( Body.class, ( a, f ) -> {
            f.apply( a, null );
            return REQUEST_BODY;
        } );
        registerResolvers( Header.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return HEADER;
        } );
        registerResolvers( Session.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return SESSION;
        } );
        registerResolvers( Context.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return CONTEXT;
        } );
        registerResolvers( Cookie.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return COOKIE;
        } );
        registerResolvers( Attribute.class, ( a, f ) -> {
            f.apply( a, a.value() );
            return ATTRIBUTE;
        } );
    }

    public boolean isLiteral() {
        switch ( this ) {
            case ATTRIBUTE:
                return false;
            case REQUEST_PARAM:
                return true;
            case REQUEST_BODY:
                return false;
            case HEADER:
                return true;
            case SESSION:
                return false;
            case CONTEXT:
                return false;
            case COOKIE:
                return true;
        }
        throw new IllegalStateException( "shouldn't reach here" );
    }

    public <T extends Annotation> Class<?> getScopeDefaultType( T t ) {
        switch ( this ) {
            case ATTRIBUTE:
                return Object.class;
            case REQUEST_PARAM:
                return String.class;
            case REQUEST_BODY:
                return null != t && ( (Body) t ).binary() ? byte[].class : String.class;
            case HEADER:
                return String.class;
            case SESSION:
                return Object.class;
            case CONTEXT:
                return Object.class;
            case COOKIE:
                return Object.class;
        }
        throw new IllegalStateException( "shouldn't reach here" );
    }

    public <T extends Annotation> Object resolveByScope( RequestContext requestContext, T t, Class<?> paramType, String paramKey ) {
        Object value = null;
        switch ( this ) {
            case REQUEST_PARAM:
                value = requestContext.getParam( paramKey );
                break;
            case REQUEST_BODY:
                value = getBody( requestContext, paramType, (Body) t );
                break;
            case HEADER:
                value = requestContext.getHeader( paramKey );
                break;
            case SESSION:
                value = requestContext.getSession().getAttribute( paramKey );
                break;
            case CONTEXT:
                value = requestContext.getContextObject( paramKey );
                break;
            case COOKIE: {
                javax.servlet.http.Cookie ck = requestContext.getCookie( paramKey );
                value = null != ck ? ck.getValue() : null;
                break;
            }
            case ATTRIBUTE: {
                value = requestContext.getAttribute( paramKey );
            }
            break;
        }
        return value;
    }

    @SuppressWarnings( "unchecked" )
    private Object getBody( RequestContext context, Class<?> type, Body body ) {
        Pair<byte[], String> cached = (Pair<byte[], String>) context.getAttribute( Body.class );
        if ( null == cached ) {
            cached = new Pair<>();
            try {
                cached.first = Util.toByteArray( context.getRequest().getInputStream() );
                context.setAttribute( Body.class, cached );
            } catch ( IOException e ) {
                throw new IllegalStateException( "receive request body error.", e );
            }
        }
        if ( byte[].class != type && (null == body || !body.binary()) ) {
            if ( null == cached.second ) {
                String enc = null != body ? body.encoding() : "utf-8";
                try {
                    cached.second = new String( cached.first, enc );
                } catch ( UnsupportedEncodingException e ) {
                    throw new IllegalStateException( "encoding( " + enc + " ) is not supported which declared in (" + Body.class.getName() + ").", e );
                }
            }
            return cached.second;
        } else {
            return cached.first;
        }
    }

    public static ParamScope resolveParamScope( Parameter parameter, BiFunction<Annotation, String, Void> metaInfoCallback ) {
        Annotation[] annotations = parameter.getAnnotations();
        if ( null != annotations && annotations.length > 0 ) {
            ParamScope resolved = null;
            for ( Annotation a : annotations ) {
                ParamScope inner = resolve( a, metaInfoCallback );
                if ( null == resolved && null != inner ) {
                    resolved = inner;
                } else if ( null != inner ) {
                    throw new ScopeResolveException( "multiple scope annotation specified on parameter: " + parameter.toString() );
                }
            }
            if ( null != resolved ) {
                return resolved;
            }
        }
        return DEFAULT_SCOPE;
    }

}
