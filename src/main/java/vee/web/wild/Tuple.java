package vee.web.wild;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public class Tuple<A, B, C, D> implements Serializable {

    private static final long serialVersionUID = 1645589381271908243L;
    public A first;
    public B second;
    public C third;
    public D fourth;

    public Tuple( A first, B second ) {
        this.first = first;
        this.second = second;
    }

    public Tuple( A first, B second, C third ) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Tuple( A first, B second, C third, D fourth ) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
}
