package top.aion0573.mqtt.utils;


import java.security.MessageDigest;
import java.util.*;

/**
 * @包名 top.aion0573.utils
 * @名称 StringUtils
 * @描述 字符串工具类
 * @创建者 AION
 * @创建时间 2018-05-24 15:05
 * @修改人 AION
 * @修改时间 2018-05-24 15:05
 * @版本 1.0
 **/
public class StringUtils {

    public static Integer getIndex(String str, char c) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) {
                return i;
            }
        }
        return null;
    }


    /**
     * 字符串转换成数组  分隔符','
     *
     * @param arrayString
     * @return
     */
    public static String[] toArray(String arrayString) {
        return toArray(arrayString, ",");
    }

    /**
     * 字符串转换成数组
     *
     * @param arrayString
     * @param regex
     * @return
     */
    public static String[] toArray(String arrayString, String regex) {
        return arrayString.split(regex);
    }


    /**
     * 组转字符串
     *
     * @param array
     * @return
     */
    public static String toArrayString(String[] array) {
        String regex = ",";
        return toArrayString(array, regex);
    }


    /**
     * 数组转字符串
     *
     * @param array
     * @param regex
     * @return
     */
    public static String toArrayString(String[] array, String regex) {
        StringBuilder stringBuilder = new StringBuilder();
        int arrayLength = array.length;
        for (int index = 0; index < arrayLength; index++) {
            stringBuilder.append(array[index]);
            if (index != arrayLength - 1) {
                stringBuilder.append(regex);
            }
        }
        return stringBuilder.toString();
    }


    /**
     * 判断字符串是否为null值
     *
     * @param str
     * @return true 是  false 否
     */
    public static boolean isNull(String str) {
        return str == null;
    }

    /**
     * 判断字符串是否不为null值
     *
     * @param str
     * @return true 不是null  false 是null
     */
    public static boolean isNotNull(String str) {
        return str != null;
    }

    /**
     * 判断字符串是否为空字符串  即""
     *
     * @param str
     * @return true 是  false 否
     */
    public static boolean isEnpty(String str) {
        return !isNull(str) && str.length() == 0;
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str
     * @return true 不是空白  false 是空白
     */
    public static boolean isBlank(String str) {
        return !isNull(str) && str.trim().length() == 0;
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str
     * @return true 不是空白  false 是空白
     */
    public static boolean isNotBlank(String str) {
        return !isNull(str) && str.trim().length() > 0;
    }

    /**
     * 判断多个字符串是否全不为空白
     *
     * @param strings
     * @return true 不是空白  false 是空白
     */
    public static boolean isNotBlanks(String... strings) {
        if (strings == null) {
            return false;
        }
        for (String s : strings) {
            if (isBlank(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串的长度是否大于指定长度  不包括等于
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean gLength(String str, int length) {
        if (str.length() > length) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的长度是否大于等于指定长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean geLength(String str, int length) {
        if (str.length() >= length) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的长度是否小于指定长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean lLength(String str, int length) {
        if (str.length() < length) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的长度是否小于等于指定长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean leLength(String str, int length) {
        if (str.length() <= length) {
            return true;
        }
        return false;
    }


    /**
     * 判断字符串的长度是否等于等于指定长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean eqLength(String str, int length) {
        if (str.length() == length) {
            return true;
        }
        return false;
    }


    /**
     * 判断字符串的长度是否不等于等于指定长度
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean neLength(String str, int length) {
        if (str.length() != length) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的长度是否在指定的俩个长度之间 包括边界值
     *
     * @param str
     * @param length1
     * @param length2
     * @return
     */
    public static boolean isBetweenLength(String str, int length1, int length2) {
        int minLength, maxLength;
        if (length1 > length2) {
            minLength = length2;
            maxLength = length1;
        } else {
            minLength = length1;
            maxLength = length2;
        }
        if (geLength(str, minLength) && leLength(str, maxLength)) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的长度是否在指定的俩个长度之间 包括边界值
     *
     * @param str
     * @param length1
     * @param length2
     * @return
     */
    public static boolean isNotBetweenLength(String str, int length1, int length2) {
        int minLength, maxLength;
        if (length1 > length2) {
            minLength = length2;
            maxLength = length1;
        } else {
            minLength = length1;
            maxLength = length2;
        }
        if (geLength(str, minLength) && leLength(str, maxLength)) {
            return false;
        }
        return true;
    }

    /**
     * 判断俩个字符串是否相等
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean isEqual(String str1, String str2) {
        return str1.equals(str2);
    }

    /**
     * 字符串数组是否包含指定字符串
     *
     * @param str
     * @param strings
     * @return
     */
    public static boolean isContain(String str, String... strings) {
        for (String s : strings) {
            if (str.equals(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 字符串数组是否不包含指定字符串
     *
     * @param str
     * @param strings
     * @return
     */
    public static boolean isNotContain(String str, String... strings) {
        for (String s : strings) {
            if (str.equals(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 集合中是否包含指定字符串
     *
     * @param str
     * @param strings
     * @return
     */
    public static boolean isContain(String str, Collection<String> strings) {
        for (String s : strings) {
            if (str.equals(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 随机字符串    返回是  0-9 a-z  A-Z
     *
     * @param length
     * @return
     */
    public static String getRandomStrings(int length) {
        char[] chars = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        };
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length);
            stringBuilder.append(chars[index]);
        }
        return stringBuilder.toString();
    }

    /**
     * 随机字符串
     *
     * @param length
     * @return
     */
    public static String getRandomStrings(char[] chars, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length);
            stringBuilder.append(chars[index]);
        }
        return stringBuilder.toString();
    }

    public static String getRandomStringsByNumber(int length) {
        char[] number = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        };
        return getRandomStrings(number, length);
    }


    /**
     * 判断是否为q
     *
     * @param qq
     * @return
     */
    public static boolean isQQ(String qq) {
        String regex = "[1-9][0-9]{4,14}";
        return qq.matches(regex);
    }

    /**
     * 大写首字母
     *
     * @param string
     * @return
     */
    public static String upperFristChar(String string) {
        char[] charArray = string.toCharArray();
        charArray[0] -= 32;
        return String.valueOf(charArray);
    }

    /**
     * map 参数转url keyvalue字符串
     *
     * @param parameterMap
     * @return
     */
    public static String toUrlKeyValue(Map<String, String> parameterMap) {
        StringBuilder stringBuilder = new StringBuilder();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            Iterator<Map.Entry<String, String>> iterator = parameterMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(entry.getValue());
                if (iterator.hasNext()) {
                    stringBuilder.append("&");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * MD5编码
     *
     * @param origin 原始字符串
     * @return 经过MD5加密之后的结果
     */
    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(resultString.getBytes("UTF-8"));
            resultString = byteArrayToHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }

    public static String byteToHexString(byte b) {
        String hexStr = Integer.toHexString(Byte.toUnsignedInt(b));
        if (hexStr.length() == 1) {
            hexStr = "0" + hexStr;
        }
        return hexStr;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        return byteArrayToHexString(bytes, null);
    }

    public static String byteArrayToHexString(byte[] bytes, String regex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(byteToHexString(b));
            if (regex != null) {
                stringBuilder.append(regex);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 小时转换成24小时制字符串
     *
     * @param hours
     * @return
     */
    public static String hoursTo24hString(Integer hours) {
        if (hours == null || hours < 0) {
            return null;
        }
        if (hours < 10) {
            return "0" + hours + ":00";
        } else {
            return hours + ":00";
        }
    }

    /**
     * 获取无符号的UUID
     *
     * @return
     */
    public static String getNoSymbolUUID() {
        String s = UUID.randomUUID().toString();
        return s.replace("-", "");
    }

    public static String concat(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

}
