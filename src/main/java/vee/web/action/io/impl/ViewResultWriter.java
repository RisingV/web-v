package vee.web.action.io.impl;

import vee.web.action.io.View;
import vee.web.action.reflect.result.ResultGetter;
import vee.web.action.reflect.result.ResultWriter;
import vee.web.servlet.RequestContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-26  <br/>
 */
public class ViewResultWriter implements ResultWriter {

    private static Logger log = LoggerFactory.getLogger( ViewResultWriter.class );

    static class SingleViewEngineInstance {
        static ViewEngine instance = new ViewEngine();
    }

    static class ViewEngine implements LogChute {

        private VelocityEngine engine;

        {
            engine = new VelocityEngine();
            engine.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
            engine.setProperty( "resource.loader", "class" );
            engine.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        }

        public VelocityEngine engine() {
            return engine;
        }

        @Override
        public void init( RuntimeServices rs ) throws Exception {

        }

        @Override
        public void log( int level, String message ) {
            switch ( level ) {
                case TRACE_ID:
                    log.trace( TRACE_PREFIX + message );
                    break;
                case DEBUG_ID:
                    log.debug( DEBUG_PREFIX + message );
                    break;
                case INFO_ID:
                    log.info( INFO_PREFIX + message );
                    break;
                case WARN_ID:
                    log.warn( WARN_PREFIX + message );
                    break;
                case ERROR_ID:
                    log.error( ERROR_PREFIX + message );
                    break;
            }
        }

        @Override
        public void log( int level, String message, Throwable t ) {
            switch ( level ) {
                case TRACE_ID:
                    log.trace( TRACE_PREFIX + message, t );
                    break;
                case DEBUG_ID:
                    log.debug( DEBUG_PREFIX + message, t );
                    break;
                case INFO_ID:
                    log.info( INFO_PREFIX + message, t );
                    break;
                case WARN_ID:
                    log.warn( WARN_PREFIX + message, t );
                    break;
                case ERROR_ID:
                    log.error( ERROR_PREFIX + message, t );
                    break;
            }
        }

        @Override
        public boolean isLevelEnabled( int level ) {
            switch ( level ) {
                case TRACE_ID: return log.isTraceEnabled();
                case DEBUG_ID: return log.isDebugEnabled();
                case INFO_ID: return log.isInfoEnabled();
                case WARN_ID: return log.isWarnEnabled();
                case ERROR_ID: return log.isErrorEnabled();
            }
            return false;
        }

    }

    @Override
    public void write( ResultGetter getter, RequestContext context ) {
        View view = (View) getter.get();
        String mimeType = null != view.getMimeType() ? view.getMimeType() : "text/html";
        String enc = null != view.getEncoding() ? view.getEncoding() : "utf-8";
        Template template = SingleViewEngineInstance.instance.engine().getTemplate( view.getTemplateName(), enc );
        VelocityContext vc = new VelocityContext();

        vc.put( view.getModelName(), view.getModel() );

        HttpServletResponse resp = context.getResponse();
        resp.setContentType( mimeType );
        try {
            PrintWriter writer = resp.getWriter();
            template.merge( vc, writer );
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }

    }

}
