package vee.web.action.reflect.param;

import vee.web.action.reflect.param.resolve.*;
import vee.web.action.tag.Optional;
import vee.web.exception.InvalidParamTypeException;
import vee.web.exception.ParamLostException;
import vee.web.exception.ParamResolveException;
import vee.web.servlet.RequestContext;
import vee.web.wild.Ref;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-19  <br/>
 */
public class ParamMeta {

    private final static Map<Class<?>, List<MethodNode>> methodNodesCache = new HashMap<>();

    private final String paramKey;
    private final Class<?> type;
    private final ParamScope scope;
    private final boolean optional;
    private final Annotation scopeAnnotation;
    private final Annotation[] annotations;
    private final boolean typeCompatibility;
    private final boolean isEnum;

    private volatile Annotation resolverAnnotation;
    private volatile boolean canBeCast = false;
    private volatile boolean canResolveByAnnotated = false;
    private volatile boolean canResolveByAnnotatedTried = false;

    private ParamMeta( String paramKey, Class<?> type, ParamScope scope, boolean optional,
                       Annotation scopeAnnotation, Annotation[] annotations ) {
        this.paramKey = paramKey;
        this.type = type;
        this.scope = scope;
        this.typeCompatibility = type.isAssignableFrom( scope.getScopeDefaultType( scopeAnnotation ) );
        this.optional = optional;
        this.scopeAnnotation = scopeAnnotation;
        this.annotations = annotations;
        this.isEnum = type.isEnum();
    }

    public Object resolveValue( RequestContext requestContext, ParamResolverContainer paramResolverContainer ) throws ParamLostException {

        if ( canResolveByAnnotated ) {
            return optionalCheck( resolveByAnnotated( requestContext, paramResolverContainer, resolverAnnotation ) );
        } else if ( !canResolveByAnnotatedTried && testIsCanResolveByAnnotated( paramResolverContainer ) ) {
            canResolveByAnnotated = true;
            canResolveByAnnotatedTried = true;
            return optionalCheck( resolveByAnnotated( requestContext, paramResolverContainer, resolverAnnotation ) );
        }

        PreTypeResolver preTypeResolver = paramResolverContainer.getPreTypeResolver( type );
        if ( null != preTypeResolver ) {
            return optionalCheck( preTypeResolver.resolve( requestContext, paramKey, scope, type ) );
        }

        Object value = scope.resolveByScope( requestContext, scopeAnnotation, type, paramKey );
        ParamResolver resolver = paramResolverContainer.getParamResolver( paramKey );
        TypeCaster typeCaster = paramResolverContainer.getTypeCaster( paramKey );
        if ( null == value || ( null == resolver && null == typeCaster && typeCompatibility ) ) {
            return optionalCheck( value );
        }

        if ( scope.isLiteral() ) {
            String raw = (String) value;

            if ( null != resolver ) {
                value = resolver.resolve( raw );
            } else {
                if ( isEnum ) {
                    return optionalCheck( resolveEnum( raw ) );
                }
                InnerParamResolver innerResolver = paramResolverContainer.getInnerParamResolver( type );
                if ( null != innerResolver ) {
                    value = innerResolver.resolve( raw );
                } else {
                    throw new ParamResolveException( "Can't resolve parameter + '" + paramKey + "' with type: " + type.getName()
                            + ", no proper parameter resolver." );
                }
            }
        }

        if ( null != typeCaster ) {
            value = typeCaster.cast( value );
        }

        //cache reflect result
        if ( canBeCast ) {
            return optionalCheck( type.cast( value ) );
        }

        Class<?> valueType = value.getClass();
        if ( ( canBeCast = type.isAssignableFrom( valueType ) ) ) {
            return optionalCheck( type.cast( value ) );
        } else if ( type.isPrimitive() ) {
            return optionalCheck( value );
        } else {
            throw new InvalidParamTypeException( "Can't resolve value for parameter + '" + paramKey + "', " + valueType.getName() +
                    " can't cast to " + type.getName() );
        }

    }

    private Object optionalCheck( Object value ) {
        if ( null == value && !optional ) {
            throw new ParamLostException( "Can't find parameter: " + paramKey );
        }
        return value;
    }

    private Object resolveEnum( String value ) {
        for ( Object constant : type.getEnumConstants() ) {
            if ( constant.toString().equals( value ) ) {
                return constant;
            }
        }
        throw new ParamResolveException( "Can't convert '" + value + "' to enum: " + type.getName() );
    }

    @SuppressWarnings( "unchecked" )
    private <T extends Annotation> Object resolveByAnnotated( RequestContext requestContext, ParamResolverContainer paramResolverContainer, T annotated ) {
        Class<T> annotationType = (Class<T>) annotated.annotationType();
        return paramResolverContainer.getAnnotatedResolver( annotationType ).resolve( requestContext, annotated, scope, paramKey, type );
    }

    private boolean testIsCanResolveByAnnotated( ParamResolverContainer paramResolverContainer ) {
        if ( null != annotations && annotations.length > 0 ) {
            for ( Annotation ann : annotations ) {
                if ( null != paramResolverContainer.getAnnotatedResolver( ann.annotationType() ) ) {
                    if ( null == resolverAnnotation ) {
                        resolverAnnotation = ann;
                    } else {
                        throw new ParamResolveException( "multiple annotated resolvers found." );
                    }
                }
            }
        }
        return null != resolverAnnotation;
    }

    public static ParamMeta[] buildParamMetaInfoArray( Method m ) {
        Parameter[] parameters = m.getParameters();
        ParamMeta[] metaInfoArray = new ParamMeta[parameters.length];
        for ( int i = 0; i < metaInfoArray.length; ++i ) {
            metaInfoArray[i] = resolveParamMeta( m.getDeclaringClass(), m, i, parameters[i] );
        }
        return metaInfoArray;
    }

    public static ParamMeta resolveParamMeta( Class<?> declaringClass, Method m, int paramIndex, Parameter parameter ) {
        Class<?> type = parameter.getType();
        Ref<String> paramKey = new Ref<>();
        Ref<Annotation> scopeAnnotation = new Ref<>();
        ParamScope paramScope = ParamScope.resolveParamScope( parameter, ( scopeAnnotated, name ) -> {
            scopeAnnotation.value = scopeAnnotated;
            paramKey.value = name;
            return null;
        } );
        boolean isOptional = ( null != parameter.getAnnotation( Optional.class ) );

        if ( null == paramKey.value ) {
            List<MethodNode> methodNodes = getMethodNodes( declaringClass );
            paramKey.value = resolveLocalParamVariableName( methodNodes, m, paramIndex );
        }

        return new ParamMeta( paramKey.value, type, paramScope, isOptional, scopeAnnotation.value, parameter.getAnnotations() );
    }

    private static List<MethodNode> getMethodNodes( Class<?> declaringClass ) {
        List<MethodNode> methodNodes = methodNodesCache.get( declaringClass );
        if ( null == methodNodes ) {
            synchronized ( methodNodesCache ) {
                if ( !methodNodesCache.containsKey( declaringClass ) ) {
                    methodNodes = resolveMethodNodes( declaringClass );
                    methodNodesCache.put( declaringClass, methodNodes );
                }
            }
        }
        return methodNodes;
    }

    private static List<MethodNode> resolveMethodNodes( Class<?> declaringClass ) {
        ClassLoader declaringClassLoader = declaringClass.getClassLoader();
        Type declaringType = Type.getType( declaringClass );
        String url = declaringType.getInternalName() + ".class";
        InputStream classFileInputStream = declaringClassLoader.getResourceAsStream( url );
        if ( classFileInputStream == null ) {
            throw new IllegalStateException( "loading bytecode failed, URL: " + url );
        }

        ClassNode classNode;
        try {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader( classFileInputStream );
            classReader.accept( classNode, 0 );
        } catch ( IOException e ) {
            throw new ParamResolveException( "class reader loading bytecode failed, URL: " + url );
        } finally {
            try {
                classFileInputStream.close();
            } catch ( IOException ignored ) {
            }
        }

        @SuppressWarnings( "unchecked" )
        List<MethodNode> methods = classNode.methods;
        return methods;
    }

    private static String resolveLocalParamVariableName( List<MethodNode> methodNodes, Method targetMethod, int paramIndex ) {
        String targetMethodDescriptor = Type.getMethodDescriptor( targetMethod );
        int targetIndex = paramIndex + 1;

        for ( MethodNode methodNode : methodNodes ) {
            if ( targetMethodDescriptor.equals( methodNode.desc ) && targetMethod.getName().equals( methodNode.name ) ) {
                @SuppressWarnings( "unchecked" )
                List<LocalVariableNode> localVariables = methodNode.localVariables;
                if ( null != localVariables ) {
                    for ( LocalVariableNode lvn : localVariables ) {
                        if ( null != lvn && lvn.index == targetIndex ) {
                            return lvn.name;
                        }
                    }
                }
            }
        }

        throw new IllegalStateException( "lookup local parameter variable name failed!" );
    }

}
