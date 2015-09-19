package vee.web.action.reflect.result;

import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public interface ResultWriter {

    void write( ResultGetter getter, RequestContext context );

}
