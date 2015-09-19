package vee.web.action.reflect.action.filter;

import vee.web.action.reflect.action.AccessPoint;
import vee.web.action.reflect.action.filter.impl.Result;
import vee.web.action.reflect.result.ResultWriterResolver;
import vee.web.servlet.RequestContext;
import vee.web.wild.Null;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public class FilteredAccess<A extends Annotation> implements AccessPoint {

    private FilterChainGetter<A> preFilterChainGetter;
    private FilterChainGetter<A> postFilterChainGetter;
    private AccessPoint accessPoint;

    public FilteredAccess( AccessPoint accessPoint, FilterChainGetter<A> preFilterChainGetter, FilterChainGetter<A> postFilterChainGetter ) {
        this.accessPoint = accessPoint;
        this.preFilterChainGetter = preFilterChainGetter;
        this.postFilterChainGetter = postFilterChainGetter;
    }

    @Override
    public Object call( Object action, RequestContext requestContext ) {

        FilterChain<A> preFilterChain = preFilterChainGetter.getChain();
        FilterPoint<A> fp = preFilterChain.next();
        Object result, filtered;

        final AtomicBoolean fs = new AtomicBoolean( true );
        requestContext.setAttribute( FilterStatus.class, (FilterStatus) fs::set );

        if ( null != fp ) {
            do {
                filtered = fp.filter.apply( fp.filterAnnotation, action, fp.accessPoint, requestContext );
                if ( Null.aNull == filtered ) continue;
                if ( fs.get() ) {
                    requestContext.setAttribute( fp.resultType, filtered );
                } else {
                    requestContext.setAttribute( ResultWriterResolver.class, fp.resultType );
                    return filtered;
                }

            } while ( null != ( fp = preFilterChain.next() ) );
        }

        if ( null != postFilterChainGetter ) {
            result = accessPoint.call( action, requestContext );
            FilterChain<A> postFilterChain = postFilterChainGetter.getChain();
            fp = postFilterChain.next();

            if ( null == fp ) return result;
            requestContext.setAttribute( Result.class, result );

            do {
                filtered = fp.filter.apply( fp.filterAnnotation, action, fp.accessPoint, requestContext );
                if ( Null.aNull == filtered ) continue;
                if ( fs.get() ) {
                    requestContext.setAttribute( fp.resultType, filtered );
                } else {
                    requestContext.setAttribute( ResultWriterResolver.class, fp.resultType );
                    return filtered;
                }

            } while ( null != ( fp = postFilterChain.next() ) );

            return ( Null.aNull != filtered ) ? filtered : result;
        } else {
            return accessPoint.call( action, requestContext );
        }

    }

}
