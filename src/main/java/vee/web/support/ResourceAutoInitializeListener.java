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
public class ResourceAutoInitializeListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger( ResourceAutoInitializeListener.class );

    @Override
    public void contextInitialized( ServletContextEvent servletContextEvent ) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext( servletContext );
        Map<String, AutoInitializable> toInitializes = springContext.getBeansOfType( AutoInitializable.class );
        if ( null != toInitializes ) {
            for ( Map.Entry<String, AutoInitializable> en : toInitializes.entrySet() ) {
                doInit( en.getKey(), en.getValue() );
            }
        }
    }

    private void doInit( String name, AutoInitializable ai ) {
        try {
            ai.initialize();
        } catch ( Exception e ) {
            log.error( "initialize resource error, beanName: {}, detail: {}.", name, e );
        }
    }

    @Override
    public void contextDestroyed( ServletContextEvent servletContextEvent ) {
    }

}
