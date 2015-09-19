package vee.web.action.reflect.result;

import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public interface ResultWriterResolver {

    ResultWriter resolve( Object action, RequestContext context );

}
