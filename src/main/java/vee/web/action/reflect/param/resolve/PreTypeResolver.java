package vee.web.action.reflect.param.resolve;

import vee.web.action.reflect.param.ParamScope;
import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public interface PreTypeResolver {

    Object resolve( RequestContext context, String paramKey, ParamScope scope, Class<?> type );

}
