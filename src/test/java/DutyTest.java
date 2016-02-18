import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by jinyingshi on 2016/2/5.
 */
public class DutyTest {

    ApplicationContext context;

    @Before
    public void setUp() throws Exception {
        String path = "D:\\java\\IdeaProjects\\sns-monitor-server\\src\\main\\env\\config\\test\\zk.json";
        ZkUtils.setZkConfigFilePath(path);
        ZkUtils.initZkConfig(path);
        context = new ClassPathXmlApplicationContext("classpath:monitor/monitor-spring.xml");
    }

    @Test
    public void sendTest() throws Exception {
        SelectPersonService bean = context.getBean(SelectPersonService.class);
        for (int i = 0; i < 10; i++) {
            bean.send();

        }
    }
}
