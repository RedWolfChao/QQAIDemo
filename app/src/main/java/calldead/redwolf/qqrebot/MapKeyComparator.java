package calldead.redwolf.qqrebot;

import java.util.Comparator;

/**
 * 排序器 按照Map的key以字典顺序排序
 */
public class MapKeyComparator implements Comparator<String> {

    @Override
    public int compare(String str1, String str2) {

        return str1.compareTo(str2);
    }
}