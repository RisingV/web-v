package vee.web.wild;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public class Pair<A, B> implements Serializable {

    private static final long serialVersionUID = 1773927086352908102L;

    public Pair() {
    }

    public Pair( A a, B b ) {
        first = a;
        second = b;
    }

    public A first;
    public B second;

}
