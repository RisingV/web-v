package vee.web.action;

import vee.web.action.reflect.action.DefaultActionInvoker;
import vee.web.action.tag.Action;
import vee.web.exception.ScanException;
import vee.web.scan.Scanner;
import vee.web.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA. <br/>
 * User: francis    <br/>
 * Date: 13-11-5    <br/>
 * Time: 15:02  <br/>
 */
public class ActionLoadListener implements ServletContextListener, WebConstants {

    private static Logger log = LoggerFactory.getLogger( ActionLoadListener.class );

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        final ServletContext servletContext = sce.getServletContext();
        final String actionPkg = servletContext.getInitParameter( ACTION_PACKAGE );

        if ( null == actionPkg || actionPkg.isEmpty() ) {
            errorHandle( null, "'gee.web.action.package' is invalid!" );
            return;
        }

        Map<String, Object> actions;
        try {

            actions = Scanner.scanAndInstantiate( actionPkg, Action.class, Action::value,
                    Thread.currentThread().getContextClassLoader() );

        } catch ( ScanException | ClassNotFoundException | IOException e ) {
            errorHandle( e, "scan actions failed!" );
            return;
        }

        final ActionContainer actionContainer = new ActionContainer0( actions );
        servletContext.setAttribute( ACTION_CONTAINER, actionContainer );
        servletContext.setAttribute( ACTION_CONTAINER_RAW, actions );

        DefaultActionInvoker.getInstance().preBuild( actionContainer );
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {

    }

    private void errorHandle( Exception err, String reason ) {
        String msg = "Action load listener start failed! due to: " + reason;
        if ( null == err ) {
            throw new RuntimeException( msg );
        } else {
            throw new RuntimeException( msg, err );
        }
    }

    static class ActionContainer0 implements ActionContainer {

        private final Map<String, Object> actions;
        private volatile Set<String> names;

        public ActionContainer0( Map<String, Object> actions ) {
            this.actions = Collections.unmodifiableMap( actions );
        }

        @Override
        public Set<String> actionNames() {
            if ( null == names ) {
                synchronized ( this ) {
                    if ( null == names ) {
                        names = new HashSet<>( actions.keySet() );
                    }
                }
            }
            return names;
        }

        @Override
        public Object getAction( String name ) {
            Object action = actions.get( name );
            if ( null == action ) {
                log.error( "can't find action named: " + name );
            }
            return action;
        }

    }

}
