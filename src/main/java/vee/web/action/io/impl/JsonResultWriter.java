package vee.web.action.io.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vee.web.action.reflect.result.ResultGetter;
import vee.web.action.reflect.result.ResultWriter;
import vee.web.servlet.RequestContext;
import vee.web.util.Util;
import vee.web.util.WebConstants;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public class JsonResultWriter implements ResultWriter {

    private static final Logger log = LoggerFactory.getLogger( JsonResultWriter.class );

    private static SerializeConfig defaultSerializeConfig = new SerializeConfig();

    static {
        defaultSerializeConfig.put( Date.class, new SimpleDateFormatSerializer( WebConstants.GLOBAL_DATE_FORMAT ) );
    }

    private PropertyPreFilter propertyPreFilter;
    private SerializeConfig serializeConfig;

    public static SerializeConfig getDefaultSerializeConfig() {
        return defaultSerializeConfig;
    }

    public static void setDefaultSerializeConfig( SerializeConfig defaultSerializeConfig ) {
        JsonResultWriter.defaultSerializeConfig = defaultSerializeConfig;
    }

    public PropertyPreFilter getPropertyPreFilter() {
        return propertyPreFilter;
    }

    public void setPropertyPreFilter( PropertyPreFilter propertyPreFilter ) {
        this.propertyPreFilter = propertyPreFilter;
    }

    public SerializeConfig getSerializeConfig() {
        return null == serializeConfig ? getDefaultSerializeConfig() : serializeConfig;
    }

    public void setSerializeConfig( SerializeConfig serializeConfig ) {
        this.serializeConfig = serializeConfig;
    }

    @Override
    public void write( ResultGetter getter, RequestContext context ) {
        Object result;
        int status = 0;
        String msg = null;
        try {
            result = getter.get();
        } catch ( Exception err ) {
            result = "";
            status = -1;
            msg = Util.getStackTrace( err );
        }

        SerializeConfig config = getSerializeConfig();
        PropertyPreFilter filter = getPropertyPreFilter();

        String resultJSON;
        JsonWrapper decorated = new JsonWrapper( result, status, msg );
        if ( null != config && null == filter ) {
            resultJSON = JSON.toJSONString( decorated, config );
        } else if ( null != config ) {
            resultJSON = toJSONString( decorated, config, filter );
        } else if ( null != filter ) {
            resultJSON = JSON.toJSONString( decorated, filter );
        } else {
            resultJSON = JSON.toJSONString( decorated );
        }

        HttpServletResponse resp = context.getResponse();
        resp.setContentType( "text/plain" );
        try {
            PrintWriter writer = resp.getWriter();
            writer.write( resultJSON );
        } catch ( IOException e ) {
            log.error( "write result error:", e );
        }

    }

    public static class JsonWrapper {

        private Object response;
        private int status;
        private String msg;

        public JsonWrapper( Object response, int status, String msg ) {
            this.response = response;
            this.status = status;
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg( String msg ) {
            this.msg = msg;
        }

        public Object getResponse() {
            return response;
        }

        public void setResponse( Object response ) {
            this.response = response;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus( int status ) {
            this.status = status;
        }
    }

    //fastjson JSON Object has no method fits such parameters, so create one like this.
    public static String toJSONString( Object object, SerializeConfig config, SerializeFilter filter,
                                       SerializerFeature... features ) {
        SerializeWriter out = new SerializeWriter();

        try {
            JSONSerializer serializer = new JSONSerializer( out, config );
            for ( com.alibaba.fastjson.serializer.SerializerFeature feature : features ) {
                serializer.config( feature, true );
            }

            serializer.config( SerializerFeature.WriteDateUseDateFormat, true );

            if ( filter != null ) {
                if ( filter instanceof PropertyPreFilter ) {
                    serializer.getPropertyPreFilters().add(
                            (PropertyPreFilter) filter );
                }

                if ( filter instanceof NameFilter ) {
                    serializer.getNameFilters().add( (NameFilter) filter );
                }

                if ( filter instanceof ValueFilter ) {
                    serializer.getValueFilters().add( (ValueFilter) filter );
                }

                if ( filter instanceof PropertyFilter ) {
                    serializer.getPropertyFilters()
                            .add( (PropertyFilter) filter );
                }
            }

            serializer.write( object );

            return out.toString();
        } finally {
            out.close();
        }
    }

}
