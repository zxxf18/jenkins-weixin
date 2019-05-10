package me.sherry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.sherry.model.WXData;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// ref : https://work.weixin.qq.com/api/doc#90000/90135/90236
public class WXSender {
    private static final String MSG_TYPE_TEXT = "text";
    private static final String CONTENT_KEY = "content";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=";
    private static final String MSG_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long MAX_TIME = 30 * 60 * 1000;

    private static Map<String, Object> tokenMap = new HashMap<String, Object>();

    /**
     * 微信授权请求，GET类型，获取授权响应，用于其他方法截取token
     *
     * @param tokenUrl
     * @return String 授权响应内容
     * @throws IOException
     */
    private static String auth(String tokenUrl) {
        String resp = "";
        HttpGet httpGet;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpGet = new HttpGet(tokenUrl);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }

    /**
     * @param corpId     应用组织编号
     * @param corpSecret 应用秘钥
     */
    public static String getToken(String corpId, String corpSecret) {
        if (check()) {
            return tokenMap.get("access_token").toString();
        }
        String resp = auth(getTokenUrl(corpId, corpSecret));
        JSONObject json = JSON.parseObject(resp);
        String token = json.getString("access_token");
        tokenMap.put("time", System.currentTimeMillis() + MAX_TIME);
        tokenMap.put("access_token", token);
        return token;
    }

    private static boolean check() {
        return tokenMap != null
                && tokenMap.containsKey("access_token")
                && (Long) (tokenMap.get("time")) > System.currentTimeMillis();

    }

    public static String send(String data, String token) {
        String resp = "";
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(MSG_URL + token);
            httpPost.setEntity(new StringEntity(data, "utf-8"));
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }

    /**
     * @param toUser        如果有多个用户需要进行推送,用|分开  如:zhangsan|lisi 即可向两人分别进行推送;
     *                      这个值为 @all  即为向所有成员进行推送
     * @param applicationId
     * @param departmentId
     * @param contentValue
     * @return
     */
    public static String createBody(String toUser,
                                    int applicationId,
                                    String departmentId,
                                    String chatId,
                                    String contentValue) {
        WXData data = new WXData();
        data.setTouser(toUser);
        data.setMsgtype(MSG_TYPE_TEXT);
        data.setToparty(departmentId);
        data.setChatid(chatId);
        data.setAgentid(applicationId);
        Map<Object, Object> content = new HashMap<Object, Object>();
        content.put(CONTENT_KEY, contentValue + "\n\n" + dateFormat.format(new Date()));
        data.setText(content);
        return JSONObject.toJSONString(data);
    }

    private static String getTokenUrl(String corpId, String corpSecret) {
        return TOKEN_URL + corpId + "&corpsecret=" + corpSecret;
    }

    public static void main(String[] args) {
        String token = WXSender.getToken("wweebfaa2b51a28243",
                "JMRQ_E0nT9lfRR_87KGhNmZ6_cpAArC7g2DUCCQy_lI");
        String body = WXSender.createBody("wenxiaofang",
                1000042,
                "",
                "AslanAlert",
                "test");
        String resp = WXSender.send(body, token);

        System.out.println(token);
        System.out.println(body);
        System.out.println(resp);
    }

}
