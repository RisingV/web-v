package vee.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-06  <br/>
 */
public class ResourceAutoReleaseListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger( ResourceAutoReleaseListener.class );

    private Map<String, AutoCloseable> toReleases;

    @Override
    public void contextInitialized( ServletContextEvent servletContextEvent ) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext( servletContext );
        toReleases = springContext.getBeansOfType( AutoCloseable.class );
    }

    @Override
    public void contextDestroyed( ServletContextEvent servletContextEvent ) {
        doRelease( toReleases );
        doRelease( ClosableRegistry.toCloses );
    }

    private void doRelease( Map<String, AutoCloseable> toReleases ) {
        if ( null != toReleases ) {
            for ( Map.Entry<String, AutoCloseable> en : toReleases.entrySet() ) {
                try {
                    log.info( "closing resource: {} ", en.getKey() );
                    en.getValue().close();
                } catch ( Exception e ) {
                    log.error( "global resource release error, resourceName: {}, detail: {} ", en.getKey(), e );
                }
            }
        }
    }

}
