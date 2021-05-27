package com.siggytech.utils.communication.model.async;

import android.content.Context;

import com.google.gson.Gson;
import com.siggytech.utils.communication.model.EventMessageModel;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.model.GroupRequestModel;
import com.siggytech.utils.communication.model.PairRegisterModel;
import com.siggytech.utils.communication.model.QueueRequestModel;
import com.siggytech.utils.communication.model.RegisterModel;
import com.siggytech.utils.communication.presentation.MessengerHelper;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.FileUtil;
import com.siggytech.utils.communication.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.siggytech.utils.communication.presentation.register.Siggy.DEVICE_TOKEN;

public class ApiService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public ApiService() {

    }

    private static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.connectTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String registerDevice(RegisterModel model, Context context) throws Exception{
        String deviceToken = null;
        OkHttpClient.Builder clientSSL = getUnsafeOkHttpClient();
        Gson gson =  Utils.getGson();
        try{
            RequestBody body = RequestBody.create(JSON,gson.toJson(model));
            Request request = new Request.Builder()
                    .url("http://" + Conf.SERVER_IP + ":" + Conf.SERVER_REGISTER_DEVICE + "/registerdevice")
                    .post(body)
                    .build();
            Response response = clientSSL.build().newCall(request).execute();
            if(response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                deviceToken = jsonObject.getString("token");
                FileUtil.writeToFile(DEVICE_TOKEN,deviceToken,context);
            }
        }finally {
            clientSSL = null;
            gson = null;
        }

        return deviceToken;
    }

    public TaskMessage getGroups(String deviceToken, String apiKey){
        TaskMessage taskMessage = new TaskMessage();
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String url = "http://" + Conf.SERVER_IP + ":" + Conf.SERVER_IMAGE_PORT + "/getgroupsfordevice?iddevice=" + deviceToken + "&API_KEY=" + apiKey;

            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                JSONArray jsonArray = new JSONArray(EntityUtils.toString(respEntity));

                for(int i=0; i<jsonArray.length();i++){
                    MessengerHelper.getGroupList().add(new GroupModel((jsonArray.getJSONObject(i)).getInt("idgroup"), (jsonArray.getJSONObject(i)).getString("name")));
                }

                taskMessage.setMessage("Get Groups Success");
            }
        } catch (Exception e) {
            taskMessage.setMessage("Get Groups Failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }

        return taskMessage;
    }

    public TaskMessage setFirebaseToken(PairRegisterModel model) {
        TaskMessage taskMessage = new TaskMessage();
        OkHttpClient.Builder clientSSL = getUnsafeOkHttpClient();
        Gson gson =  Utils.getGson();
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(model));
            Request request = new Request.Builder()
                    .url("http://" + Conf.SERVER_IP + ":" + Conf.SERVER_IMAGE_PORT + "/pairtoken")
                    .post(body)
                    .build();
            Response response = clientSSL.build().newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                taskMessage.setMessage(jsonObject.getString("message"));
            }

        }catch (Exception e){
            taskMessage.setMessage("Set pair token failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
            clientSSL = null;
            gson = null;
        }

        return taskMessage;
    }

    public TaskMessage getChatQueue(QueueRequestModel model) {
        TaskMessage taskMessage = new TaskMessage();
        OkHttpClient.Builder clientSSL = getUnsafeOkHttpClient();
        Gson gson =  Utils.getGson();
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(model));
            Request request = new Request.Builder()
                    .url("http://" + Conf.SERVER_IP + ":" + Conf.SERVER_IMAGE_PORT + "/getchatqueue")
                    .post(body)
                    .build();
            Response response = clientSSL.build().newCall(request).execute();
            if (response.isSuccessful()) {

                JSONArray jsonArray =  new JSONArray(response.body().string());
                if(jsonArray.length()>0){
                    List<EventMessageModel> list = new ArrayList<>();
                    for(int i=0; i<jsonArray.length();i++){
                        Utils.traces(gson.fromJson(jsonArray.get(i).toString(),EventMessageModel.class).toString());
                        list.add(gson.fromJson(jsonArray.get(i).toString(),EventMessageModel.class));
                    }
                    MessengerHelper.setChatQueue(list);
                }

            }
        }catch (Exception e){
            Utils.traces(Utils.exceptionToString(e));
            taskMessage.setMessage("Get Queue chat failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
            clientSSL = null;
            gson = null;
        }

        return taskMessage;
    }

    public TaskMessage getPttGroups(GroupRequestModel model) {
        TaskMessage taskMessage = new TaskMessage();
        Gson gson = Utils.getGson();
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/getgroups?imei=" + model.getDeviceToken() + "&API_KEY=" + model.getApiKey();

            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                JSONObject jsonObject = new JSONObject(EntityUtils.toString(respEntity));
                JSONArray jsonArray = jsonObject.getJSONArray("groups");

                for(int i=0; i<jsonArray.length();i++){
                    MessengerHelper.getPttGroupList().add(gson.fromJson(jsonArray.get(i).toString(),GroupModel.class));
                }

                taskMessage.setMessage("Get Ptt Groups Success");
            }
        } catch (Exception e) {
            taskMessage.setMessage("Get Ptt Groups Failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
            Utils.traces("get ppt groups: "+Utils.exceptionToString(e));
        }
        finally {
            gson = null;
        }

        return taskMessage;
    }
}
