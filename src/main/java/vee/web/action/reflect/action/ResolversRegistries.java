package vee.web.action.reflect.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import vee.web.action.io.FileDownload;
import vee.web.action.io.FilesUpload;
import vee.web.action.io.View;
import vee.web.action.io.impl.FileResultWriter;
import vee.web.action.io.impl.FilesUploadParamResolver;
import vee.web.action.io.impl.JsonResultWriter;
import vee.web.action.io.impl.ViewResultWriter;
import vee.web.action.reflect.action.filter.FilterStatus;
import vee.web.action.reflect.action.filter.impl.FilterFor;
import vee.web.action.reflect.action.filter.impl.Result;
import vee.web.action.reflect.action.filter.mgr.DefaultFilterMgr;
import vee.web.action.reflect.action.filter.mgr.FilterMgr;
import vee.web.action.reflect.param.resolve.DefaultParamResolverMgr;
import vee.web.action.reflect.param.resolve.ParamResolverMgr;
import vee.web.action.reflect.result.DefaultResultWriterMgr;
import vee.web.action.reflect.result.ResultWriterMgr;
import vee.web.action.tag.convert.JsonArray;
import vee.web.action.tag.convert.JsonObject;
import vee.web.exception.InvalidParamTypeException;
import vee.web.exception.ParamResolveException;
import vee.web.servlet.RequestContext;
import vee.web.wild.Null;
import vee.web.util.Util;
import vee.web.util.WebConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public final class ResolversRegistries {

    private static final ThreadLocal<SimpleDateFormat> threadOwnedFormat = new ThreadLocal<>();

    private static SimpleDateFormat getDateFormat() {
        SimpleDateFormat sdf = threadOwnedFormat.get();
        if ( null == sdf ) {
            synchronized ( threadOwnedFormat ) {
                if ( null == threadOwnedFormat.get() ) {
                    sdf = new SimpleDateFormat( WebConstants.GLOBAL_DATE_FORMAT );
                    threadOwnedFormat.set( sdf );
                }
            }
        }
        return sdf;
    }

    private ResolversRegistries() {
    }

    private static volatile boolean done = false;

    public static synchronized void doRegistries() {
        if  ( done ) return;
        try {
            doRegistries0();
        } finally {
            done = true;
        }
    }

    private static void doRegistries0() {
        ParamResolverMgr paramResolverMgr = DefaultParamResolverMgr.sharedInstance();

        paramResolverMgr.registerInnerParamResolver( byte.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Byte.valueOf( raw ) : Byte.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Byte.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Byte.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( short.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Short.valueOf( raw ) : Short.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Short.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Short.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( int.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Integer.valueOf( raw ) : Integer.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Integer.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Integer.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( long.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Long.valueOf( raw ) : Long.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Long.class,
                ( raw ) -> null != raw && Util.isNumeric( raw ) ? Long.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( float.class,
                ( raw ) -> null != raw && Util.isDecimal( raw ) ? Float.valueOf( raw ) : Float.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Float.class,
                ( raw ) -> null != raw && Util.isDecimal( raw ) ? Float.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( double.class,
                ( raw ) -> null != raw && Util.isDecimal( raw ) ? Double.valueOf( raw ) : Double.MIN_VALUE );
        paramResolverMgr.registerInnerParamResolver( Double.class,
                ( raw ) -> null != raw && Util.isDecimal( raw ) ? Double.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( boolean.class, Boolean::valueOf );
        paramResolverMgr.registerInnerParamResolver( Boolean.class,
                ( raw ) -> null != raw ? Boolean.valueOf( raw ) : null );

        paramResolverMgr.registerInnerParamResolver( char.class,
                ( raw ) -> null != raw && !raw.isEmpty() ? raw.charAt( 0 ) : (char) ( 0x00 ) );
        paramResolverMgr.registerInnerParamResolver( Character.class,
                ( raw ) -> null != raw && !raw.isEmpty() ? raw.charAt( 0 ) : null );

        paramResolverMgr.registerInnerParamResolver( String.class, ( raw ) -> raw );

        paramResolverMgr.registerInnerParamResolver( Date.class, ( raw ) -> {
            try {
                return getDateFormat().parse( raw );
            } catch ( ParseException e ) {
                throw new InvalidParamTypeException( "unaccepted date format: " + raw );
            }
        } );

        paramResolverMgr.registerPreTypeResolver( RequestContext.class, ( context, paramKey, scope, type ) -> context );
        paramResolverMgr.registerPreTypeResolver( HttpServletRequest.class, ( context, paramKey, scope, type ) -> context.getRequest() );
        paramResolverMgr.registerPreTypeResolver( HttpServletResponse.class, ( context, paramKey, scope, type ) -> context.getResponse() );
        paramResolverMgr.registerPreTypeResolver( FilterStatus.class, ( context, paramKey, scope, type ) -> context.getAttribute( FilterStatus.class ) );
        paramResolverMgr.registerPreTypeResolver( FilesUpload.class, new FilesUploadParamResolver() );
        paramResolverMgr.registerPreTypeResolver( JSONArray.class, ( context, paramKey, scope, type ) -> {
            Object value = scope.resolveByScope( context, null, type, paramKey );
            if ( null == value ) return null;
            if ( scope.isLiteral() || value instanceof String ) {
                return JSONArray.parseArray( (String) value );
            } else {
                throw new ParamResolveException( "can't convert '" + value.getClass().getName() + "' to '" + JSONArray.class.getName() + "'." );
            }
        } );
        paramResolverMgr.registerPreTypeResolver( JSONObject.class, ( context, paramKey, scope, type ) -> {
            Object value = scope.resolveByScope( context, null, type, paramKey );
            if ( null == value ) return null;
            if ( scope.isLiteral() || value instanceof String ) {
                return JSONObject.parseObject( (String) value );
            } else {
                throw new ParamResolveException( "can't convert '" + value.getClass().getName() + "' to '" + JSONObject.class.getName() + "'." );
            }
        } );

        paramResolverMgr.registerAnnotatedResolver( Result.class, ( context, result, s, paramKey, type ) -> context.getAttribute( Result.class ) );
        paramResolverMgr.registerAnnotatedResolver( JsonArray.class, ( context, jsonArray, scope, paramKey, type ) -> {
            Object value = scope.resolveByScope( context, null, type, paramKey );
            if ( null == value ) return null;
            if ( scope.isLiteral() || value instanceof String ) {
                if ( type == List.class ) {
                    return JSONArray.parseArray( (String) value, jsonArray.type() );
                } else if ( type == JSONArray.class ) {
                    return JSON.parseArray( (String) value );
                }
            }
            throw new ParamResolveException( "can't convert '" + value.getClass().getName() +
                    "' to json array, with element type '" + jsonArray.type().getName() + "'." );
        } );
        paramResolverMgr.registerAnnotatedResolver( JsonObject.class, ( context, jsonObject, scope, paramKey, type ) -> {
            Object value = scope.resolveByScope( context, null, type, paramKey );
            if ( null == value ) return null;
            if ( scope.isLiteral() || value instanceof String ) {
                if ( type == JSONObject.class ) {
                    return JSONObject.parseObject( (String) value );
                } else {
                    return JSONObject.parseObject( (String) value, type );
                }
            }
            throw new ParamResolveException( "can't convert '" + value.getClass().getName() +
                    "' to type '" + type.getName() + "' through json parsing." );
        } );

        ResultWriterMgr resultWriterMgr = DefaultResultWriterMgr.sharedInstance();
        resultWriterMgr.registerResultWriter( FileDownload.class, new FileResultWriter() );
        resultWriterMgr.registerResultWriter( View.class, new ViewResultWriter() );
        resultWriterMgr.setDefault( new JsonResultWriter() );

        FilterMgr filterMgr = DefaultFilterMgr.sharedInstance();

        filterMgr.registerFilter( FilterFor.class, ( filterAnnotation, action, accessPoint, context ) -> {
            String path = context.getPath();
            int sepIndex = path.indexOf( '/', 1 );
            if ( sepIndex > 0 ) {
                String callPath = path.substring( sepIndex + 1 );
                if ( callPath.matches( filterAnnotation.value() ) ) {
                    return accessPoint.call( action, context );
                }
            }
            return Null.aNull;
        } );
    }

}
