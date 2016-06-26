/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.spat.scf.serializer.test.serializer;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.spat.scf.serializer.serializer.Serializer;
import org.spat.scf.serializer.test.entity.SESUser;
/**
 *
 * @author Administrator
 */
public class EnterpriseUserTest {

     @Test
    public void TestUser() throws Exception
    {
        SESUser user = new SESUser();
        user.setUserID(1L);
        user.setState(1);

        Serializer serializer = new Serializer();
        byte[] buffer = serializer.Serialize(user);
        assertNotNull(buffer);
        Object obj = serializer.Derialize(buffer, SimpleClass.class);
        Object expect = obj;
        assertNotNull(expect);
    }
}