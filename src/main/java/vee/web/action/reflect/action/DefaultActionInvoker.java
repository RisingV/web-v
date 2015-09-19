package vee.web.action.reflect.action;

import com.esotericsoftware.reflectasm.MethodAccess;
import vee.web.action.ActionContainer;
import vee.web.action.reflect.action.async.BackendAccess;
import vee.web.action.reflect.action.async.BackendTask;
import vee.web.action.reflect.action.async.CarryOnTask;
import vee.web.action.reflect.action.async.ExecuteType;
import vee.web.action.reflect.action.filter.Filter;
import vee.web.action.reflect.action.filter.FilterChainGetter;
import vee.web.action.reflect.action.filter.FilterPoint;
import vee.web.action.reflect.action.filter.FilteredAccess;
import vee.web.action.reflect.action.filter.impl.FilterFor;
import vee.web.action.reflect.action.filter.impl.FilterOrder;
import vee.web.action.reflect.action.filter.mgr.DefaultFilterMgr;
import vee.web.action.reflect.action.filter.mgr.FilterContainer;
import vee.web.action.reflect.action.filter.mgr.FilterRegistry;
import vee.web.action.reflect.param.ParamMeta;
import vee.web.action.reflect.param.resolve.DefaultParamResolverMgr;
import vee.web.action.reflect.param.resolve.ParamResolverContainer;
import vee.web.action.reflect.param.resolve.ParamResolverRegistry;
import vee.web.action.reflect.result.*;
import vee.web.action.tag.Backend;
import vee.web.action.tag.Path;
import vee.web.exception.UnknownPathException;
import vee.web.servlet.RequestContext;
import vee.web.util.Util;
import vee.web.wild.Null;
import vee.web.wild.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public class DefaultActionInvoker implements ActionInvoker {

    private static final Logger log = LoggerFactory.getLogger( DefaultActionInvoker.class );
    private static final int BLACK_LIST_PATH_LIMIT = 1000;

    static {
        ResolversRegistries.doRegistries();
    }

    static class LazyLoader {
        private final static ActionInvoker instance = new DefaultActionInvoker();
    }

    public static ActionInvoker getInstance() {
        return LazyLoader.instance;
    }

    private final Map<String, Tuple<AccessPoint, Object, ResultWriterResolver, Boolean>> actionAccessCache = new HashMap<>();
    private final Map<String, FilterChainGetter<?>> filterChainsCache = new HashMap<>();
    private final Set<String> blackList = new HashSet<>( BLACK_LIST_PATH_LIMIT );

    @Override
    public void invoke( ActionContainer actionContainer, RequestContext context ) {
        String path = context.getPath();
        Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tuple = getActionAccess( path, actionContainer );
        //for debug.
        if ( null == tuple ) {
            throw new IllegalStateException( "---------------- tuple is null !!!!!" );
        } else if ( null == tuple.fourth ) {
            throw new IllegalStateException( "---------------- tuple.fourth is null !!!!!" );
        }
        if ( tuple.fourth ) {
            syncCall( tuple, context );
        } else {
            asyncCall( tuple, context );
        }
    }

    @Override
    public void preBuild( ActionContainer actionContainer ) {
        long ms = System.currentTimeMillis();
        for ( String name : actionContainer.actionNames() ) {
            Set<String> subPathSet = extractSubPathSet( name, actionContainer.getAction( name ) );
            log.info( "building access points of action: {},", name );
            for ( String subPath : subPathSet ) {
                String path = '/' + name + '/' + subPath;
                log.info( "     building access point of path: {} ", path );
                try {
                    getActionAccess( path, actionContainer );
                } catch ( Exception e ) {
                    log.error( "error when building access point of path: {} ", path );
                    throw new IllegalStateException( e );
                }
            }
        }
        log.info( "building access points time cost: {} ms.", System.currentTimeMillis() - ms );
    }

    private Set<String> extractSubPathSet( String name, Object action ) {
        Set<String> pathSet = new HashSet<>();
        Class<?> clazz = action.getClass();
        for ( Method m : clazz.getDeclaredMethods() ) {
            Path path = m.getAnnotation( Path.class );
            if ( null != path ) {
                if ( pathSet.contains( path.value() ) ) {
                    throw new IllegalStateException( "duplicated action path: /" + path.value() + " for action: " + name );
                } else {
                    pathSet.add( path.value() );
                }
            }
        }
        return pathSet;
    }

    private void syncCall( Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tuple, RequestContext context ) {
        Exception err = null;
        Object result = null;
        try {
            result = tuple.first.call( tuple.second, context );
        } catch ( Exception e ) {
            err = e;
        }
        final Tuple<Object, Exception, Null, Null> tuple1 = new Tuple<>( result, err );
        tuple.third.resolve( tuple.second, context ).write( () -> {
            if ( null != tuple1.second ) {
                throw new RuntimeException( tuple1.second );
            }
            return tuple1.first;
        }, context );
    }

    private void asyncCall( Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tuple, RequestContext context ) {
        tuple.third.resolve( tuple.second, context ).write( () -> tuple.first.call( tuple.second, context ), context );
    }

    private Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> getActionAccess( String path, ActionContainer actionContainer ) {
        PathParser pathParser = new PathParser( path );
        String wholePath;
        while ( null != pathParser.nextCallPath() ) {
            wholePath = pathParser.getCurrentWholePath();
            if ( isInBlackList( wholePath ) ) continue;
            Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tuple = actionAccessCache.get( wholePath );
            if ( null == tuple ) {
                tuple = tryResolveActionAccess( pathParser, actionContainer );
            }
            if ( null != tuple ) {
                return tuple;
            }
        }
        throw new UnknownPathException( "No handler for path: " + path );
    }

    private boolean isInBlackList( String path ) {
        return blackList.contains( path );
    }

    private void addToBlackList( String path ) {
        synchronized ( blackList ) {
            if ( blackList.size() > BLACK_LIST_PATH_LIMIT ) {
                blackList.clear();
            }
            blackList.add( path );
        }
    }

    private Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tryResolveActionAccess( PathParser pathParser, ActionContainer actionContainer ) {
        String wholePath = pathParser.getCurrentWholePath();
        Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> tuple = actionAccessCache.get( pathParser.getCurrentWholePath() );
        if ( null == tuple ) {
            synchronized ( actionAccessCache ) {
                if ( !actionAccessCache.containsKey( wholePath ) ) {
                    tuple = resolveActionAccess( pathParser, actionContainer );
                    if ( null != tuple ) {
                        actionAccessCache.put( wholePath, tuple );
                    }
                }
            }
        }
        return tuple;
    }

    private Tuple<AccessPoint, Object, ResultWriterResolver, Boolean> resolveActionAccess( PathParser pathParser, ActionContainer actionContainer ) {
        String actionName = pathParser.getActionName(), wholePath = pathParser.getCurrentWholePath(), callPath = pathParser.getCurrentCallPath();
        if ( null != actionName ) {
            Object action = actionContainer.getAction( actionName );
            if ( null != action ) {
                Tuple<AccessPoint, ResultWriterResolver, Boolean, Null> tuple = buildActionAccess( action, wholePath, callPath );
                if ( null != tuple ) {
                    return new Tuple<>( tuple.first, action, tuple.second, tuple.third );
                } else {
                    addToBlackList( wholePath );
                }
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    private Tuple<AccessPoint, ResultWriterResolver, Boolean, Null> buildActionAccess( Object action, String wholePath, String callPath ) {
        Class<?> clazz = action.getClass();
        for ( Method method : clazz.getDeclaredMethods() ) {
            Path path = method.getAnnotation( Path.class );
            if ( ( null != path && callPath.equals( path.value() ) ) || ( null == path && callPath.equals( method.getName() ) ) ) {

                AccessPoint actionAccess = buildInvokePoint( clazz, action, method );
                FilterChainGetter preFilterChainGetter = getFilterChainGetter( clazz, action, wholePath, clazz.getMethods(), FilterOrder.preFilter, Path.class );
                FilterChainGetter postFilterChainGetter = getFilterChainGetter( clazz, action, wholePath, clazz.getMethods(), FilterOrder.postFiler, Path.class );
                if ( null == preFilterChainGetter ) {
                    final ResultWriter resultWriter = getResultWriter( action, method.getReturnType() );
                    return new Tuple<>( actionAccess, ( ignored0, ignored1 ) -> resultWriter, Boolean.FALSE, Null.aNull );
                } else {

                    final ResultWriterContainer resultWriterContainer = getResultWriterContainer( action );
                    final Class<?> actionReturnType = method.getReturnType();

                    return new Tuple<>(

                            new FilteredAccess( actionAccess, preFilterChainGetter, postFilterChainGetter ),

                            (ResultWriterResolver) ( action0, context ) -> {
                                Class<?> returnType = (Class<?>) context.getAttribute( ResultWriterResolver.class );
                                if ( null == returnType ) returnType = actionReturnType;
                                return resultWriterContainer.getResultWriter( returnType );
                            },

                            Boolean.TRUE, Null.aNull );
                }

            }
        }
        return null;
    }

    private AccessPoint buildInvokePoint( Class<?> actionType, Object action, Method method ) {
        MethodAccess methodAccess = MethodAccess.get( actionType );
        ParamMeta[] metaInfoArray = ParamMeta.buildParamMetaInfoArray( method );
        ParamResolverContainer paramResolverContainer = getParamResolverRegistry( action ).getParamResolverContainer();
        int index = methodAccess.getIndex( method.getName(), method.getParameterTypes() );
        AccessPoint ap = new ActionAccess( methodAccess, index, metaInfoArray, paramResolverContainer );

        Backend backend = method.getAnnotation( Backend.class );
        if ( null == backend ) {
            return ap;
        } else {
            Class<?> rt = method.getReturnType();
            if ( rt.equals( BackendTask.class ) ) {
                return new BackendAccess( ap, backend.timeout(), ExecuteType.BACKEND_TASK );
            } else if ( rt.equals( CarryOnTask.class ) ) {
                return new BackendAccess( ap, backend.timeout(), ExecuteType.CARRY_ON_TASK );
            } else {
                return new BackendAccess( ap, backend.timeout(), ExecuteType.DUMMY );
            }
        }
    }

    @SafeVarargs
    private final FilterChainGetter<?> getFilterChainGetter( Class<?> actionType, Object action, String path, Method[] methods,
                                                             FilterOrder filterOrder, Class<? extends Annotation>... excluded ) {

        return getFilterChainGetter( actionType, action, path + filterOrder, actionType.getMethods(),

                (Function<Annotation, Boolean>) annotation -> null != annotation
                        && annotation.annotationType().equals( FilterFor.class )
                        && ( (FilterFor) annotation ).order() == filterOrder,

                (Function<Annotation, Integer>) annotation -> ( (FilterFor) annotation ).priority(),

                excluded );
    }

    @SafeVarargs
    @SuppressWarnings( "unchecked" )
    private final FilterChainGetter<?> getFilterChainGetter( Class<?> actionType, Object action, String path, Method[] methods,
                                                             Function<Annotation, Boolean> matcher,
                                                             Function<Annotation, Integer> priorityGetter,
                                                             Class<? extends Annotation>... excluded ) {
        FilterChainGetter<?> filterChainGetter = filterChainsCache.get( path );
        if ( null == filterChainGetter ) {
            final List<FilterPoint<?>> filterPoints = new ArrayList<>();
            FilterContainer fc = getFilterContainer( action );
            OUTER:
            for ( int index = 0; index < methods.length; ++index ) {
                if ( null != excluded && excluded.length > 0 ) {
                    for ( Class<? extends Annotation> exclude : excluded ) {
                        if ( null != methods[index].getAnnotation( exclude ) ) {
                            continue OUTER;
                        }
                    }

                    Annotation[] annotations = methods[index].getDeclaredAnnotations();
                    if ( null != annotations ) {
                        for ( Annotation annotation : annotations ) {
                            Filter<?> filter = fc.getFilter( annotation.annotationType() );
                            if ( null != filter && ( null == matcher || matcher.apply( annotation ) ) ) {
                                AccessPoint invokePoint = buildInvokePoint( actionType, action, methods[index] );
                                final Class<?> returnType = methods[index].getReturnType();
                                final int priority = priorityGetter.apply( annotation );
                                filterPoints.add( new FilterPoint( invokePoint, filter, annotation, index, priority, returnType ) );
                                try {
                                    getParamResolverRegistry( action ).registerPreTypeResolver( returnType,
                                            ( context, paramKey, scope, type ) -> context.getAttribute( returnType ) );
                                } catch ( Exception ignored ) {
                                }
                            }
                        }
                    }
                }
            }
            if ( !filterPoints.isEmpty() ) {
                filterPoints.sort( ( fp0, fp1 ) -> fp0.getPriority() - fp1.getPriority() );
                filterChainGetter = () -> {
                    final Iterator<? extends FilterPoint> it = filterPoints.iterator();
                    return () -> {
                        if ( it.hasNext() ) {
                            return it.next();
                        }
                        return null;
                    };
                };
                filterChainsCache.put( path, filterChainGetter );
            }
        }
        return filterChainGetter;
    }

    private ParamResolverRegistry getParamResolverRegistry( Object action ) {
        if ( action instanceof ParamResolverRegistry ) {
            return (ParamResolverRegistry) action;
        } else {
            return DefaultParamResolverMgr.sharedInstance();
        }
    }

    private FilterContainer getFilterContainer( Object action ) {
        if ( action instanceof FilterRegistry ) {
            return ( (FilterRegistry) action ).getFilterContainer();
        }
        return DefaultFilterMgr.sharedInstance();
    }

    private ResultWriterContainer getResultWriterContainer( Object action ) {
        if ( action instanceof ResultWriterRegistry ) {
            return ( (ResultWriterRegistry) action ).getResultWriterContainer();
        }
        return DefaultResultWriterMgr.sharedInstance();
    }

    private ResultWriter getResultWriter( Object action, Class<?> returnType ) {
        if ( action instanceof ResultWriterRegistry ) {
            return ( (ResultWriterRegistry) action ).getResultWriterContainer().getResultWriter( returnType );
        }
        return DefaultResultWriterMgr.sharedInstance().getResultWriter( returnType );
    }

    static class PathParser {

        private String actionName;
        private String terms[];
        private String originalPath;
        private String currentCallPath;
        private String currentWholePath;
        private int offset = -1;

        public PathParser( String path ) {
            parse( path );
        }

        private void parse( String path ) {
            originalPath = path;
            if ( null != path && !path.isEmpty() ) {
                terms = Util.split( path, '/' );
                if ( terms.length >= 1 ) {
                    actionName = terms[0];
                    offset = terms.length - 1;
                }
            }
        }

        public String getActionName() {
            return actionName;
        }

        public String getOriginalPath() {
            return originalPath;
        }

        public String getCurrentCallPath() {
            return currentCallPath;
        }

        public String getCurrentWholePath() {
            return currentWholePath;
        }

        public String nextCallPath() {
            if ( offset > 0 && offset < terms.length ) {
                StringBuilder buf = new StringBuilder();
                for ( int i = 1; i <= offset; ++i ) {
                    buf.append( terms[i] ).append( '/' );
                }
                offset--;
                currentCallPath = buf.deleteCharAt( buf.length() - 1 ).toString();
                currentWholePath = "/" + actionName + "/" + currentCallPath;
                return currentCallPath;
            }
            return null;
        }

    }

}
