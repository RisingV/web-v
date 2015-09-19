package vee.web.action.reflect.action;

import vee.web.action.ActionContainer;
import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public interface ActionInvoker {

    void invoke( ActionContainer actionContainer, RequestContext requestContext );

    void preBuild( ActionContainer actionContainer );

}
