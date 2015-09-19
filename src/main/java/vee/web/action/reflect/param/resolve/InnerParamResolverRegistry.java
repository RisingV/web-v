package vee.web.action.reflect.param.resolve;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public interface InnerParamResolverRegistry {

    void registerInnerParamResolver( String key, InnerParamResolver innerParamResolver );

    void registerInnerParamResolver( Class<?> type, InnerParamResolver innerParamResolver );

}
