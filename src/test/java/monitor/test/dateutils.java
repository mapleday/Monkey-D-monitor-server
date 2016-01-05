package monitor.test;

import com.sohu.sns.monitor.util.DateUtil;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Gary on 2016/1/4.
 */
public class dateutils {

    public static void main(String[] args) throws ParseException {
        Date date = DateUtil.getBeginDate();
        System.out.println(date);
    }
}
