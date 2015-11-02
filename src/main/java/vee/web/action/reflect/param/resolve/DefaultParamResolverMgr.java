package vee.web.action.reflect.param.resolve;

import vee.web.exception.RegisterException;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-19  <br/>
 */
public final class DefaultParamResolverMgr implements ParamResolverMgr {

    private final static class SharedParamResolverMgr {
        private static final DefaultParamResolverMgr shared = new DefaultParamResolverMgr();
    }

    public static ParamResolverMgr sharedInstance() {
        return SharedParamResolverMgr.shared;
    }

    public static ParamResolverMgr newInstance() {
        return new DefaultParamResolverMgr();
    }

    private final Map<Class<?>, InnerParamResolver> innerParamResolversT = new HashMap<>();
    private final Map<String, InnerParamResolver> innerParamResolversK = new HashMap<>();
    private final Map<String, ParamResolver> paramResolvers = new HashMap<>();
    private final Map<String, TypeCaster> typeCasters = new HashMap<>();
    private final Map<Class<?>, PreTypeResolver> preTypeResolvers = new HashMap<>();
    private final Map<Class<? extends Annotation>, AnnotatedResolver<?>> annotatedResolvers = new HashMap<>();

    private DefaultParamResolverMgr() {
    }

    @Override
    public void registerTypeCaster( String key, TypeCaster typeCaster ) {
        synchronized ( typeCasters ) {
            if ( typeCasters.containsKey( key ) ) {
                throw new RegisterException( " type caster for '" + key + "' already registered!" );
            }
            if ( null != key && null != typeCaster ) {
                typeCasters.put( key, typeCaster );
            }
        }
    }

    @Override
    public void registerPreTypeResolver( Class<?> type, PreTypeResolver preTypeResolver ) {
        synchronized ( preTypeResolvers ) {
            if ( null != getInnerParamResolver( type ) ) return;
            if ( preTypeResolvers.containsKey( type ) ) {
                throw new RegisterException( " pre-defined type resolver for type :" +
                        ( null != type ? type.getName() : "null" ) + " already registered!" );
            }
            if ( null != type && null != preTypeResolver ) {
                preTypeResolvers.put( type, preTypeResolver );
                Class<?>[] interfaces = type.getInterfaces();
                if ( null != interfaces && interfaces.length > 0 ) {
                    for ( Class<?> ifc : interfaces ) {
                        if ( null != ifc && !preTypeResolvers.containsKey( ifc ) ) {
                            preTypeResolvers.put( ifc, preTypeResolver );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerInnerParamResolver( Class<?> clazz, InnerParamResolver resolver ) {
        synchronized ( innerParamResolversT ) {
            if ( innerParamResolversT.containsKey( clazz ) ) {
                throw new RegisterException( " resolver for type: " +
                        ( null != clazz ? clazz.getName() : "null" ) + " already registered!" );
            }
            if ( null != clazz && null != resolver ) {
                innerParamResolversT.put( clazz, resolver );
            }
        }
    }

    @Override
    public void registerInnerParamResolver( String paramKey, InnerParamResolver resolver ) {
        synchronized ( innerParamResolversK ) {
            if ( innerParamResolversK.containsKey( paramKey ) ) {
                throw new RegisterException( " resolver for parameter: " + paramKey + " already registered!" );
            }
            if ( null != paramKey && null != resolver ) {
                innerParamResolversK.put( paramKey, resolver );
            }
        }
    }

    @Override
    public void registerParamResolver( String paramKey, ParamResolver resolver ) {
        synchronized ( paramResolvers ) {
            if ( paramResolvers.containsKey( paramKey ) ) {
                throw new RegisterException( " resolver for parameter: " + paramKey + " already registered!" );
            }
            if ( null != paramKey && null != resolver ) {
                paramResolvers.put( paramKey, resolver );
            }
        }
    }

    @Override
    public <A extends Annotation> void registerAnnotatedResolver( Class<A> type, AnnotatedResolver<A> annotatedResolver ) {
        synchronized ( annotatedResolvers ) {
            if ( annotatedResolvers.containsKey( type ) ) {
                String typeName = ( null != type ) ? type.getName() : "null";
                throw new RegisterException( " annotatedResolver for annotation type: " + typeName + " already registered!" );
            }
            if ( null != type && null != annotatedResolver ) {
                annotatedResolvers.put( type, annotatedResolver );
            }
        }
    }

    @Override
    public TypeCaster getTypeCaster( String key ) {
        TypeCaster typeCaster = typeCasters.get( key );
        if ( null == typeCaster ) {
            typeCaster = ( (DefaultParamResolverMgr) sharedInstance() ).getTypeCaster0( key );
        }
        return typeCaster;
    }

    @Override
    public PreTypeResolver getPreTypeResolver( Class<?> type ) {
        PreTypeResolver preTypeResolver = preTypeResolvers.get( type );
        if ( null == preTypeResolver ) {
            preTypeResolver = ( (DefaultParamResolverMgr) sharedInstance() ).getPreTypeResolver0( type );
        }
        return preTypeResolver;
    }

    @Override
    public InnerParamResolver getInnerParamResolver( Class<?> type ) {
        InnerParamResolver resolver = ( (DefaultParamResolverMgr) sharedInstance() ).getInnerParamResolver0( type );
        if ( null == resolver ) {
            resolver = innerParamResolversT.get( type );
        }
        return resolver;
    }

    @Override
    public InnerParamResolver getInnerParamResolver( String paramKey ) {
        InnerParamResolver resolver = ( (DefaultParamResolverMgr) sharedInstance() ).getInnerParamResolver0( paramKey );
        if ( null == resolver ) {
            resolver = innerParamResolversK.get( paramKey );
        }
        return resolver;
    }

    @Override
    public ParamResolver getParamResolver( String paramKey ) {
        ParamResolver resolver = paramResolvers.get( paramKey );
        if ( null == resolver ) {
            resolver = ( (DefaultParamResolverMgr) sharedInstance() ).getParamResolver0( paramKey );
        }
        return resolver;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <A extends Annotation> AnnotatedResolver<A> getAnnotatedResolver( Class<A> type ) {
        AnnotatedResolver<A> annotatedResolver = (AnnotatedResolver<A>) annotatedResolvers.get( type );
        if ( null == annotatedResolver ) {
            annotatedResolver = ( (DefaultParamResolverMgr) sharedInstance() ).getAnnotatedResolver0( type );
        }
        return annotatedResolver;
    }

    private TypeCaster getTypeCaster0( String key ) {
        return typeCasters.get( key );
    }

    private PreTypeResolver getPreTypeResolver0( Class<?> type ) {
        return preTypeResolvers.get( type );
    }

    private InnerParamResolver getInnerParamResolver0( Class<?> type ) {
        return innerParamResolversT.get( type );
    }

    private InnerParamResolver getInnerParamResolver0( String paramKey ) {
        return innerParamResolversK.get( paramKey );
    }

    private ParamResolver getParamResolver0( String paramKey ) {
        return paramResolvers.get( paramKey );
    }

    @SuppressWarnings( "unchecked" )
    private <A extends Annotation> AnnotatedResolver<A> getAnnotatedResolver0( Class<A> type ) {
        return (AnnotatedResolver<A>) annotatedResolvers.get( type );
    }

}