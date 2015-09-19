package vee.web.util;

import vee.web.exception.InvalidParamTypeException;
import vee.web.wild.Pair;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public final class Util {

    private Util() {
    }

    public static String getStackTrace( Throwable throwable ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw, true );
        throwable.printStackTrace( pw );
        return sw.getBuffer().toString();
    }

    public static boolean isNumeric( String str ) {
        int sz;
        if ( str != null && ( sz = str.length() ) > 0 ) {
            for ( int i = 0; i < sz; i++ ) {
                if ( !Character.isDigit( str.charAt( i ) ) ) {
                    break;
                }
            }
            return true;
        }

        throw new InvalidParamTypeException( "invalid integer number literal!" );
    }

    public static boolean isDecimal( String str ) {
        if ( str != null && !str.isEmpty() ) {
            return str.matches( "[0-9]*(\\.?)[0-9]*" );
        }

        throw new InvalidParamTypeException( "invalid float number literal!" );
    }

    public static String[] split( String str, char separatorChar ) {
        return splitWorker( str, separatorChar, false );
    }

    private static String[] splitWorker( String str, char separatorChar, boolean preserveAllTokens ) {

        if ( str == null ) {
            return null;
        }
        int len = str.length();
        if ( len == 0 ) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while ( i < len ) {
            if ( str.charAt( i ) == separatorChar ) {
                if ( match || preserveAllTokens ) {
                    list.add( str.substring( start, i ) );
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if ( match || ( preserveAllTokens && lastMatch ) ) {
            list.add( str.substring( start, i ) );
        }
        return list.toArray( new String[list.size()] );
    }

    public static final long INVALID_IP = -( 0x1 << 10 );

    private static final List<Pair<Long, Long>> PRIVATE_IP = new ArrayList<>();

    static {
        List<Pair<String, String>> l = new ArrayList<>();

        //l.add( new Pair<>( "0.0.0.0", "2.255.255.255" ) );
        //l.add( new Pair<>( "169.254.0.0", "169.254.255.255" ) );
        l.add( new Pair<>( "127.0.0.0", "127.255.255.255" ) );
        l.add( new Pair<>( "192.0.2.0", "192.0.2.255" ) );
        l.add( new Pair<>( "255.255.255.0", "255.255.255.255" ) );

        //below 3 defined by http://tools.ietf.org/html/rfc1918
        l.add( new Pair<>( "10.0.0.0", "10.255.255.255" ) );
        l.add( new Pair<>( "172.16.0.0", "172.31.255.255" ) );
        l.add( new Pair<>( "192.168.0.0", "192.168.255.255" ) );

        for ( Pair<String, String> ssp : l ) {
            Pair<Long, Long> llp = new Pair<>();
            llp.first = ip2longV0( ssp.first );
            llp.second = ip2longV0( ssp.second );
            PRIVATE_IP.add( llp );
        }
    }

    public static long toPublicIP( String ipStr ) {
        long ip = ip2longV0( ipStr );
        if ( INVALID_IP == ip || !ipValidation( ip ) ) {
            return INVALID_IP;
        }
        return ip;
    }

    public static boolean ipValidation( String ip ) {
        if ( null == ip ) return false;
        long ipNum = ip2longV0( ip );
        return ipValidation( ipNum );
    }

    public static boolean ipValidation( long ipNum ) {
        if ( INVALID_IP == ipNum ) return false;
        for ( Pair<Long, Long> p : PRIVATE_IP ) {
            if ( p.first <= ipNum && ipNum <= p.second ) {
                return false;
            }
        }
        return true;
    }

    //performance op/ms:20791 in test
    public static long ip2longV0( String ipStr ) {
        long ip = 0, octet = 0, move = 24, dot = -1;
        final int last = ipStr.length() - 1;
        for ( int i = 0; i <= last; i++ ) {
            final char c = ipStr.charAt( i );
            if ( '.' != c ) {
                if ( c < '0' || c > '9' ) {
                    return INVALID_IP;
                }
                octet = 10 * octet + ( c - '0' );
            }
            if ( '.' == c || i == last ) {
                //first char can't be '.'
                if ( octet < 0 || octet > 255 || 0 == i ) {
                    return INVALID_IP;
                }
                ip += octet << move;
                move -= 8;
                octet = 0;
                dot++;
            }
        }
        return 3 == dot ? ip : INVALID_IP;
    }

    //no error validation
    public static String toIPStr( final long ip ) {
        long _ip = 0xffffffffL & ip;
        return String.valueOf( 0xffL & ( _ip >> 24 ) ) + '.' +
                ( 0xffL & ( _ip >> 16 ) ) + '.' +
                ( 0xffL & ( _ip >> 8 ) ) + '.' +
                ( 0xffL & _ip );
    }

    public static long resolveIPDecimal( Map<String, String> header ) {
        return toPublicIP( resolveIPStr( header ) );
    }

    public static String resolveIPStr( Map<String, String> header ) {
        String ipStr;
        if ( ipValidation( ipStr = header.get( "HTTP_CLIENT_IP" ) ) ) {
            return ipStr;
        }
        if ( header.containsKey( "HTTP_X_FORWARDED_FOR" ) ) {
            String[] ipStrs = split( header.get( "HTTP_X_FORWARDED_FOR" ), ',' );
            if ( null != ipStrs ) {
                for ( String _ip : ipStrs ) {
                    if ( ipValidation( _ip ) ) {
                        return _ip;
                    }
                }
            }
        }
        if ( header.containsKey( "X-Forwarded-For" ) ) {
            String[] ipStrs = split( header.get( "X-Forwarded-For" ), ',' );
            if ( null != ipStrs ) {
                for ( String _ip : ipStrs ) {
                    if ( ipValidation( _ip ) ) {
                        return _ip;
                    }
                }
            }
        }
        if ( ipValidation( ipStr = header.get( "HTTP_X_FORWARDED" ) ) ) {
            return ipStr;
        }
        if ( ipValidation( ipStr = header.get( "HTTP_X_CLUSTER_CLIENT_IP" ) ) ) {
            return ipStr;
        }
        if ( ipValidation( ipStr = header.get( "HTTP_FORWARDED_FOR" ) ) ) {
            return ipStr;
        }
        if ( ipValidation( ipStr = header.get( "HTTP_FORWARDED" ) ) ) {
            return ipStr;
        }
        if ( ipValidation( ipStr = header.get( "REMOTE_ADDR" ) ) ) {
            return ipStr;
        }

        return "";
    }

    public static byte[] toByteArray( InputStream input ) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream( DEFAULT_BUFFER_INIT_SIZE );
        copy( input, output );
        return output.toByteArray();
    }

    public static int copy( InputStream input, OutputStream output ) throws IOException {
        long count = copyLarge( input, output );
        if ( count > Integer.MAX_VALUE ) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge( InputStream input, OutputStream output )
            throws IOException {
        return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
    }

    public static long copyLarge( InputStream input, OutputStream output, byte[] buffer )
            throws IOException {
        long count = 0;
        int n = 0;
        while ( EOF != ( n = input.read( buffer ) ) ) {
            output.write( buffer, 0, n );
            count += n;
        }
        return count;
    }

    private static final int DEFAULT_BUFFER_INIT_SIZE = 1024;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

}
