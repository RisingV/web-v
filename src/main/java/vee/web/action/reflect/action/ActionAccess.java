package vee.web.action.reflect.action;

import com.esotericsoftware.reflectasm.MethodAccess;
import vee.web.action.reflect.param.ParamMeta;
import vee.web.action.reflect.param.resolve.ParamResolverContainer;
import vee.web.servlet.RequestContext;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public class ActionAccess implements AccessPoint {

    private final MethodAccess access;
    private final int methodIndex;
    private final ParamMeta[] paramMetas;
    private final ParamResolverContainer paramResolverContainer;

    public ActionAccess( MethodAccess access, int methodIndex,
                         ParamMeta[] paramMetas, ParamResolverContainer paramResolverContainer ) {
        this.access = access;
        this.methodIndex = methodIndex;
        this.paramMetas = paramMetas;
        this.paramResolverContainer = paramResolverContainer;
    }

    @Override
    public Object call( Object action, RequestContext requestContext ) {
        Object[] parameters = new Object[ paramMetas.length ];

        for ( int i = 0; i < parameters.length; i++ ) {
            parameters[i] = paramMetas[i].resolveValue( requestContext, paramResolverContainer );
        }

        return access.invoke( action, methodIndex, parameters );
    }

}
