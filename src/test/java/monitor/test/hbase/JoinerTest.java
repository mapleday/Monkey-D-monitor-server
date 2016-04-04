package monitor.test.hbase;

import com.google.common.base.Joiner;

/**
 * Created by Gary Chan on 2016/4/3.
 */
public class JoinerTest {
    private static final Joiner joiner = Joiner.on("_");
    public static void main(String[] args) {
        for(int i=0; i<100; i++) {
            System.out.println(joiner.join("aa", "bb"));
        }
    }
}
