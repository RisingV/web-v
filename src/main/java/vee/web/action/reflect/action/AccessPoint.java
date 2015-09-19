package vee.web.action.reflect.action;

import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@FunctionalInterface
public interface AccessPoint {

    Object call( Object action, RequestContext requestContext );

}
