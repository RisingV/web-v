package vee.web.action.reflect.result;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
@FunctionalInterface
public interface ResultGetter<T> {
    T get();
}
