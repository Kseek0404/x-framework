package de.kseek.core.cluster;

import java.util.Arrays;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class ClusterHelper {
    public static boolean inIpWhiteList(String ip, String[] whiteList) {
        if (whiteList == null || whiteList.length == 0) {
            return true;
        }
        return Arrays.asList(whiteList).contains(ip);
    }

    public static boolean preciseInIpWhiteList(String ip, String[] whiteList) {
        if (whiteList == null || whiteList.length == 0) {
            return false;
        }
        return Arrays.asList(whiteList).contains(ip);
    }

    public static boolean inIdWhiteList(long id, int[] whiteList) {
        if (whiteList == null || whiteList.length == 0) {
            return true;
        }
        for (long wid : whiteList) {
            if (wid == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean preciseInIdWhiteList(long id, int[] whiteList) {
        if (whiteList == null) {
            return false;
        }
        for (long wid : whiteList) {
            if (wid == id) {
                return true;
            }
        }
        return false;
    }
}
