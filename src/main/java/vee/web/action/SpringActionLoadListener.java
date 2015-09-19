package vee.web.action;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-30  <br/>
 */
public class SpringActionLoadListener extends ActionLoadListener {

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        super.contextInitialized( sce );

        final ServletContext servletContext = sce.getServletContext();
        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext( servletContext );

        @SuppressWarnings( "unchecked" )
        Map<String, Object> actions = (Map<String, Object>) servletContext.getAttribute( ACTION_CONTAINER_RAW );

        for ( Object action : actions.values() ) {
            processInjection( action, springContext );
        }

    }

    public void processInjection( Object target, ApplicationContext applicationContext ) {
        AutowireCapableBeanFactory acb = applicationContext.getAutowireCapableBeanFactory();
        AutowiredAnnotationBeanPostProcessor autowiredProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredProcessor.setBeanFactory( acb );
        autowiredProcessor.processInjection( target );
    }

}
