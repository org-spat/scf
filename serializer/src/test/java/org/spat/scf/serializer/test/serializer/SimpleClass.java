/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.test.serializer;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 *
 * @author Administrator
 */
@SCFSerializable(name = "SimpleClass")
public class SimpleClass {

    public SimpleClass() {
    }
    @SCFMember
    public int UserId;
    @SCFMember
    public Date PostDate;
    @SCFMember
    public SubClass[] SubClasses;
    @SCFMember
    public ArrayList List;
    @SCFMember
    public Long[] arr;
    @SCFMember
    public State state;
    @SCFMember
    public BigDecimal num;
    @SCFMember
    public Hashtable myMap;
//    @SCFMember
//    public GList<SubClass> InfoList;
//    @SCFMember
//    public GDictionary<String, Integer> Dic;
//    @SCFMember
//    public GKeyValuePair<String,String> Kv;


    public static SimpleClass Get()
    {
        SimpleClass sc = new SimpleClass();
        sc.UserId=123;
        sc.PostDate = new Date();
        sc.SubClasses = new SubClass[2];
        sc.SubClasses[0]= new SubClass();
        sc.SubClasses[1]=sc.SubClasses[0];
        sc.SubClasses[0].Name="lxsfg";
        sc.List=new ArrayList();
        sc.List.add("123456");
        sc.List.add("rtertr");
        sc.arr = new Long[]{1L,2L,3L};
        sc.state = State.Open;
        sc.num = new BigDecimal("1.43434234523452345");
        sc.myMap = new Hashtable();
        sc.myMap.put(1, 123);
//      sc.InfoList = new GList<SubClass>(SubClass.class);
        return  sc;
    }
}

