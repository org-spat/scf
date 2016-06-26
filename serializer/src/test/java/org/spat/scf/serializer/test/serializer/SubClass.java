/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.spat.scf.serializer.test.serializer;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 *
 * @author Administrator
 */
 @SCFSerializable(name="SubClass")
 public class SubClass {
        @SCFMember
        public String Name;
//        @SCFMember
//        public Date RegDate;
    }
