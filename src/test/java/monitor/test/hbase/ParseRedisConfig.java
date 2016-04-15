package monitor.test.hbase;

import com.sohu.sns.common.utils.json.JsonMapper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gary Chan on 2016/4/15.
 */
public class ParseRedisConfig {

    static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("D:/redis_result.txt"), "UTF-8"));
        String line = null;
        Map<String, String> map = new HashMap<String, String>();
        while((line = br.readLine()) != null) {
            line = line.trim();
            String[] array = line.split("\\t");
            map.put(array[0], array[1]);
        }
        System.out.println(jsonMapper.toJson(map));
    }
}
