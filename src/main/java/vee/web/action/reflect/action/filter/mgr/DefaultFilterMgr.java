package vee.web.action.reflect.action.filter.mgr;

import vee.web.action.reflect.action.filter.Filter;
import vee.web.exception.RegisterException;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public class DefaultFilterMgr implements FilterMgr {

    static class SharedFilterMgrInstance {
        static FilterMgr shared = new DefaultFilterMgr();
    }

    public static FilterMgr sharedInstance() {
        return SharedFilterMgrInstance.shared;
    }

    public static FilterMgr newInstance() {
        return new DefaultFilterMgr();
    }

    private DefaultFilterMgr() {
    }

    private Map<Class<? extends Annotation>, Filter> filters = new HashMap<>();

    @Override
    public <A extends Annotation> Filter<A> getFilter( Class<A> annotationType ) {
        Filter<A> filter = getLocal( annotationType );
        if ( null == filter ) {
            filter = ( (DefaultFilterMgr) sharedInstance() ).getLocal( annotationType );
        }
        return filter;
    }

    @Override
    public <A extends Annotation> void registerFilter( Class<A> annotationType, Filter<A> filter ) {
        synchronized ( filters ) {
            if ( filters.containsKey( annotationType ) ) {
                throw new RegisterException( " filter for anntation: '" +
                        ( ( null != annotationType ) ? annotationType.getName() : "null" ) + "' already registered!" );
            }
            if ( null != annotationType && null != filter ) {
                filters.put( annotationType, filter );
            }
        }
    }

    @Override
    public FilterContainer getFilterContainer() {
        return this;
    }

    @SuppressWarnings( "unchecked" )
    private <A extends Annotation> Filter<A> getLocal( Class<A> annotationType ) {
        return (Filter<A>) filters.get( annotationType );
    }

}
