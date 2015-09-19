package vee.web.scan;

import vee.web.exception.ScanException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-18  <br/>
 */
public final class Scanner {

    public static <V, A extends Annotation> Map<V, Object> scanAndInstantiate( final String basePkg,
                                                                               final Class<A> annotationType,
                                                                               final AnnotationValue<V, A> annotationValue,
                                                                               final ClassLoader classLoader )
            throws ScanException, IOException, ClassNotFoundException {

        Map<V, Class<?>> classes = scan( basePkg, annotationType, annotationValue, classLoader );
        Map<V, Object> instances = new HashMap<>();
        for ( Map.Entry<V, Class<?>> e : classes.entrySet() ) {
            Class<?> clz = e.getValue();
            try {
                instances.put( e.getKey(), clz.newInstance() );
            } catch ( InstantiationException | IllegalAccessException e1 ) {
                throw new ScanException( clz.getName() + " has no public non-args constructor.", e1 );
            }
        }
        return instances;
    }

    public static <V, A extends Annotation> Map<V, Class<?>> scan( final String basePkg,
                                                                   final Class<A> annotationType,
                                                                   final AnnotationValue<V, A> annotationValue,
                                                                   final ClassLoader classLoader )
            throws ScanException, IOException, ClassNotFoundException {

        return scan( basePkg, annotationType, annotationValue, classLoader, true );
    }

    public static <V, A extends Annotation> Map<V, Class<?>> scan( final String basePkg,
                                                                   final Class<A> annotationType,
                                                                   final AnnotationValue<V, A> annotationValue,
                                                                   final ClassLoader classLoader,
                                                                   final boolean recursive )
            throws IOException, ClassNotFoundException, ScanException {

        Map<V, Class<?>> classes = new HashMap<>();

        String packageName = basePkg;
        String packageDirName = packageName.replace( '.', '/' );

        Enumeration<URL> dirs = getClassLoader( classLoader ).getResources( packageDirName );
        while ( dirs.hasMoreElements() ) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            if ( "file".equals( protocol ) ) {
                String filePath = URLDecoder.decode( url.getFile(), "UTF-8" );
                findAndAddClassesInPackageByFile( packageName, filePath, classes, annotationType, annotationValue, classLoader, recursive );
            } else if ( "jar".equals( protocol ) ) {
                JarFile jar;
                jar = ( (JarURLConnection) url.openConnection() )
                        .getJarFile();
                Enumeration<JarEntry> entries = jar.entries();
                while ( entries.hasMoreElements() ) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if ( name.charAt( 0 ) == '/' ) {
                        name = name.substring( 1 );
                    }
                    if ( name.startsWith( packageDirName ) ) {
                        int idx = name.lastIndexOf( '/' );
                        if ( idx != -1 ) {
                            packageName = name.substring( 0, idx ).replace( '/', '.' );
                        }
                        if ( ( idx != -1 ) || recursive ) {
                            if ( name.endsWith( ".class" ) && !entry.isDirectory() ) {
                                String className = name.substring( packageName.length() + 1, name.length() - 6 );
                                checkMetaInfo( ( packageName + '.' + className ), annotationType, annotationValue, classes, classLoader );
                            }
                        }
                    }
                }
            }
        }

        return classes;
    }

    private static <V, A extends Annotation> void findAndAddClassesInPackageByFile( final String packageName,
                                                                                    final String packagePath,
                                                                                    final Map<V, Class<?>> classes,
                                                                                    final Class<A> annotationType,
                                                                                    final AnnotationValue<V, A> annotationValue,
                                                                                    final ClassLoader classLoader,
                                                                                    final boolean recursive )
            throws ClassNotFoundException, ScanException {

        File dir = new File( packagePath );
        if ( !dir.exists() || !dir.isDirectory() ) {
            return;
        }
        File[] files = dir.listFiles( file -> ( recursive && file.isDirectory() )
                || ( file.getName().endsWith( ".class" ) ) );

        for ( File file : files ) {
            if ( file.isDirectory() ) {
                findAndAddClassesInPackageByFile( packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        classes,
                        annotationType,
                        annotationValue,
                        classLoader,
                        recursive );
            } else {
                String className = file.getName().substring( 0, file.getName().length() - 6 );
                checkMetaInfo( ( packageName + '.' + className ), annotationType, annotationValue, classes, classLoader );
            }
        }
    }

    private static <V, A extends Annotation> void checkMetaInfo( final String clsName,
                                                                 final Class<A> annotationType,
                                                                 final AnnotationValue<V, A> annotationValue,
                                                                 final Map<V, Class<?>> classes,
                                                                 final ClassLoader classLoader )
            throws ScanException, ClassNotFoundException {

        Class<?> cls = getClassLoader( classLoader ).loadClass( clsName );
        A annotation = cls.getAnnotation( annotationType );
        if ( null != annotation ) {
            V t = annotationValue.value( annotation );
            if ( classes.containsKey( t ) ) {
                throw new ScanException( "duplicated component: '" + t + "'" );
            } else {
                classes.put( t, cls );
            }
        }
    }

    private static ClassLoader getClassLoader( ClassLoader classLoader ) {
        return null != classLoader ? classLoader : Thread.currentThread().getContextClassLoader();
    }

}
