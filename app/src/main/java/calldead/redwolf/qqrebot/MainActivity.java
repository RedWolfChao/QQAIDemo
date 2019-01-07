package calldead.redwolf.qqrebot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    EditText mEt;
    String randomStr = getRandomString(10);
    String randomSession = getRandomString(10);
    TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEt = findViewById(R.id.mEt);
        mTv = findViewById(R.id.mTv);
    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map x
     * @return x
     */
    public Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Object> sortMap = new TreeMap<>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }

    /**
     * 随机字符串
     *
     * @param length x
     * @return x
     */
    public String getRandomString(int length) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public void sendMsg(View view) {
        final String ques = mEt.getText().toString().trim();
        mTv.append("ques : " + ques + "\n");
        mEt.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long currTime = (TimeUtils.getNowMills() / 1000);

                    //  参数组合
                    Map<String, Object> params = new HashMap<>();
                    int APP_ID = 2111114017;
                    params.put("app_id", APP_ID);
                    params.put("time_stamp", currTime);
                    params.put("nonce_str", randomStr);
                    params.put("session", randomSession);
                    params.put("question", ques);
                    //  鉴权开始
                    //  1. 将<key, value>请求参数对按key进行字典升序排序，得到有序的参数对列表N
                    Map<String, Object> resultMap = sortMapByKey(params);
                    //  2.列表N中的参数对按URL键值对的格式拼接成字符串，得到字符串T（如：key1=value1&key2=value2），
                    //  URL键值拼接过程value部分需要URL编码，URL编码算法用大写字母，例如%E8，而不是小写%e8
                    //  3. 将应用密钥以app_key为键名，组成URL键值拼接到字符串T末尾，得到字符串S（如：key1=value1&key2=value2&app_key=密钥)
                    Set<String> keySet = resultMap.keySet();
                    StringBuilder sb = new StringBuilder();
                    for (String key : keySet) {
                        Object value = resultMap.get(key);
                        sb.append("&").append(key).append("=").append(URLEncoder.encode(value + "", "UTF-8"));
                    }
                    sb.deleteCharAt(0);
                    String APP_KEY = "A2m9UfqFv30JnM2W";
                    sb.append("&app_key=").append(APP_KEY);
                    LogUtils.iTag("RedWolf md5运算之前", sb);
                    //  4. MD5运算
                    String md5Sign = EncryptUtils.encryptMD5ToString(sb.toString());
                    LogUtils.iTag("RedWolf md5Sign", md5Sign);
                    //  利用Http发送对话请求
                    String URL = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat";
                    //  拼接参数之前
                    LogUtils.iTag("RedWolf", APP_ID, randomStr, ques, randomSession, currTime, APP_KEY, md5Sign);
                    OkGo.<String>get(URL)
                            .tag(this)
                            .params("app_id", APP_ID)
                            .params("nonce_str", randomStr)
                            .params("question", ques)
                            .params("session", randomSession)
                            .params("time_stamp", currTime)
                            .params("sign", md5Sign)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(final Response<String> response) {
                                    LogUtils.iTag("RedWolfR", response.body());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FUCK fuck = new Gson().fromJson(response.body(), FUCK.class);
                                            mTv.append("ans : " + fuck.getData().getAnswer());
                                            mTv.append("session : " + fuck.getData().getSession() + "\n");
                                        }
                                    });

                                }

                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    LogUtils.iTag("RedWolfR", response.body());
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();

    }

    public void clearAns(View view) {
        mTv.setText("");
    }
}
