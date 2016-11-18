package monitor;

import com.sohu.sns.monitor.dubbo.domain.DubboInvoke;
import com.sohu.sns.monitor.dubbo.util.DubboMonitorUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by yzh on 2016/11/18.
 */
public class TestDubboMonitorUtil {
    private DubboInvoke dubboInvoke;
    @Before
    public void preCheckDubboInvoke(){
        dubboInvoke = new DubboInvoke();
        dubboInvoke.setFailure(1);
    }

    @Test
    public void testCheckDubboInvoke(){
        System.out.println(dubboInvoke.getFailure());
        DubboMonitorUtil.checkDubboInvoke(dubboInvoke);
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
