package vee.web.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-18  <br/>
 */
public interface RequestContext {

    String getHttpMethod();

    String getPath();

    String getParam( String key );

    String getHeader( String key );

    Cookie getCookie( String key );

    HttpSession getSession();

    HttpServletRequest getRequest();

    HttpServletResponse getResponse();

    Object getContextObject( String key );

    Object getAttribute( String key );

    void setAttribute( String key, Object attr );

    Object getAttribute( Class<?> key );

    void setAttribute( Class<?> key, Object attr );


}
