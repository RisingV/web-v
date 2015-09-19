package vee.web.action.reflect.action.async;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-15  <br/>
 */
public interface CarryOnTask<T> extends BackendTask, PreResult {

    T get();

    T execute( long timeoutMs );

}
