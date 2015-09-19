package vee.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vee.web.action.ActionContainer;
import vee.web.action.reflect.action.ActionInvoker;
import vee.web.action.reflect.action.DefaultActionInvoker;
import vee.web.exception.Expected;
import vee.web.util.Util;
import vee.web.util.WebConstants;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * User: francis    <br/>
 * Date: 13-11-5    <br/>
 */

public class RouterServlet extends HttpServlet implements WebConstants {

    private static Logger log = LoggerFactory.getLogger( RouterServlet.class );

    private ActionInvoker actionInvoker = DefaultActionInvoker.getInstance();
    private ActionContainer actionContainer;

    @Override
    public void init() throws ServletException {
        actionContainer = (ActionContainer) getServletContext().getAttribute( WebConstants.ACTION_CONTAINER );
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {
        doAny( req, resp );
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {
        doAny( req, resp );
    }

    private void doAny( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException {

        RequestContext requestContext = new RequestContext() {

            private Map<String, Object> attributesN;
            private Map<Class<?>, Object> attributesT;

            @Override
            public String getHttpMethod() {
                return req.getMethod();
            }

            @Override
            public String getPath() {
                return req.getPathInfo();
            }

            @Override
            public String getParam( String key ) {
                return req.getParameter( key );
            }

            @Override
            public String getHeader( String key ) {
                return req.getHeader( key );
            }

            @Override
            public Cookie getCookie( String key ) {
                Cookie[] cookies = req.getCookies();
                if ( null != cookies ) {
                    for ( Cookie ck : cookies ) {
                        if ( ck.getName().equals( key ) ) {
                            return ck;
                        }
                    }
                }
                return null;
            }

            @Override
            public HttpSession getSession() {
                return req.getSession();
            }

            @Override
            public HttpServletRequest getRequest() {
                return req;
            }

            @Override
            public HttpServletResponse getResponse() {
                return resp;
            }

            @Override
            public Object getContextObject( String key ) {
                return req.getServletContext().getAttribute( key );
            }

            @Override
            public Object getAttribute( String key ) {
                return null != attributesN ? attributesN.get( key ) : null;
            }

            @Override
            public void setAttribute( String key, Object attr ) {
                if ( null == attributesN ) {
                    attributesN = new HashMap<>();
                }
                attributesN.put( key, attr );
            }

            @Override
            public Object getAttribute( Class<?> key ) {
                return null != attributesT ? attributesT.get( key ) : null;
            }

            @Override
            public void setAttribute( Class<?> key, Object attr ) {
                if ( null == attributesT ) {
                    attributesT = new HashMap<>();
                }
                attributesT.put( key, attr );
            }

        };

        try {
            invokeAction( actionContainer, requestContext );
        } catch ( Exception err ) {
            responseInvalid( resp.getWriter(), "internal error: " + err.getMessage() );
            if ( err instanceof Expected ) {
                log.error( "Global Web Layer Error, path: {}, expected error: {}", requestContext.getPath(), err.getMessage() );
            } else {
                log.error( "Global Web Layer Error, path: {}, unexpected error: ", requestContext.getPath(), Util.getStackTrace( err ) );
            }
        }

    }

    private void invokeAction( ActionContainer actionContainer, RequestContext requestContext ) {
        actionInvoker.invoke( actionContainer, requestContext );
    }

    private void responseInvalid( PrintWriter printWriter, String msg ) {
        printWriter.print( msg );
        printWriter.flush();
    }

}
