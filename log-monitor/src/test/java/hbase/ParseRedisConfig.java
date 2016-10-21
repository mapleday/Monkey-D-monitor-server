package hbase;

import com.sohu.sns.common.utils.json.JsonMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        while((line = br.readLine()) != null) {
            line = line.trim();
            String[] array = line.split("\\t");
            Map<String, String> temp = new HashMap<String, String>();
            temp.put("passwd", array[1]);
            temp.put("desc", array[2]);
            map.put(array[0], temp);
        }
        System.out.println(jsonMapper.toJson(map));
    }
}
