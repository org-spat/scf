package org.spat.scf.client.utility;

/**
 * ArrayHelper
 *
 * @author Service Platform Architecture Team 
 */
public class ArrayHelper {

    public static boolean equals(byte[] array1, byte[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] array1, int offset, byte[] array2) {
        if (array1 == array2 && offset == 0) {
            return true;
        }
        if (array1.length - offset < array2.length) {
            return false;
        }
        for (int i = 0; i < array2.length; i++) {
            if (array1[i + offset] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] subArray(byte[] source, int start, int len) {
        if (start < 0) {
            start = 0;
        }
        if (len < 0 || len > source.length) {
            return null;
        }
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = source[start + i];
        }
        return result;
    }

    public static void leftMove(byte[] array, int count) {
        for (int i = 0; i < array.length; i++) {
            int target = i - count;
            if (target < 0) {
                continue;
            }
            array[target] = array[i];
            array[i] = 0;
        }
    }
}
