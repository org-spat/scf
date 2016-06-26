package org.spat.scf.client.utility;

/**
 * CharHelper
 *
 * @author Service Platform Architecture Team 
 */
public class CharHelper {

    public static String subString(String source, int startIndex, int count) {
        if (source == null || source.equals("")) {
            return null;
        }
        if (source.length() - startIndex > count) {
            count = source.length() - startIndex;
        }
        if (startIndex <= 0) {
            startIndex = 0;
        }
        return source.substring(startIndex, count);
    }
}
