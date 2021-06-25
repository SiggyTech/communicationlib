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

    private HttpClient httpClient;
    private String url;
    private HttpPost httpPost;
    private List<NameValuePair> params;
    private HttpResponse response;
    private HttpEntity respEntity;
    private OkHttpClient.Builder clientSSL;
    private Gson gson;
    private RequestBody body;
    private Request request;

    private void clearInstances() {
        httpClient = null;
        url = null;
        httpPost = null;
        if(params!=null){
            params.clear();
            params = null;
        }
        response = null;
        respEntity = null;
        clientSSL = null;
        gson = null;
    }

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
        }else{
            Utils.traces("Response registerDevice failed");
        }

        return deviceToken;
    }

    public TaskMessage getGroups(String deviceToken, String apiKey){
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.setApiEnum(ApiEnum.GET_GROUPS);
        try {
            httpClient = new DefaultHttpClient();
            url = "http://" + Conf.SERVER_IP + ":" + Conf.SERVER_IMAGE_PORT + "/getgroupsfordevice?iddevice=" + deviceToken + "&API_KEY=" + apiKey;

            httpPost = new HttpPost(url);
            params = new ArrayList<>();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = httpClient.execute(httpPost);
            respEntity = response.getEntity();

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
        } finally {
            clearInstances();
        }

        return taskMessage;
    }



    public TaskMessage setFirebaseToken(PairRegisterModel model) {
        TaskMessage taskMessage = new TaskMessage();
        clientSSL = getUnsafeOkHttpClient();
        gson =  Utils.getGson();
        try {
            body = RequestBody.create(JSON, gson.toJson(model));
            request = new Request.Builder()
                    .url("http://" + Conf.SERVER_IP + ":" + Conf.SERVER_IMAGE_PORT + "/pairtoken")
                    .post(body)
                    .build();
            Response response = clientSSL.build().newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                taskMessage.setMessage(jsonObject.getString("message"));
            }else{
                Utils.traces("Response setFirebaseToken failed");
            }

        }catch (Exception e){
            taskMessage.setMessage("Set pair token failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
           clearInstances();
        }

        return taskMessage;
    }

    public TaskMessage getChatQueue(QueueRequestModel model) {
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.setApiEnum(ApiEnum.GET_CHAT_QUEUE);
        clientSSL = getUnsafeOkHttpClient();
        gson =  Utils.getGson();
        try {
            body = RequestBody.create(JSON, gson.toJson(model));
            request = new Request.Builder()
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
            }else{
                Utils.traces("Response Get Queue chat failed");
            }

        }catch (Exception e){
            Utils.traces("Get Queue chat failed: "+Utils.exceptionToString(e));
            taskMessage.setMessage("Get Queue chat failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
           clearInstances();
        }

        return taskMessage;
    }

    public TaskMessage getPttGroups(GroupRequestModel model) {
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.setApiEnum(ApiEnum.GET_PTT_GROUPS);
        gson = Utils.getGson();
        try {
            httpClient = new DefaultHttpClient();
            url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/getgroups?imei=" + model.getDeviceToken() + "&API_KEY=" + model.getApiKey();

            httpPost = new HttpPost(url);
            params = new ArrayList<>();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response = httpClient.execute(httpPost);
            respEntity = response.getEntity();

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
            clearInstances();
        }

        return taskMessage;
    }

    public TaskMessage setFirebaseTokenPtt(PairRegisterModel model) {
        TaskMessage taskMessage = new TaskMessage();
        clientSSL = getUnsafeOkHttpClient();
        gson =  Utils.getGson();
        try {
            body = RequestBody.create(JSON, gson.toJson(model));
            request = new Request.Builder()
                    .url("http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/pairtoken")
                    .post(body)
                    .build();
            Response response = clientSSL.build().newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                taskMessage.setMessage(jsonObject.getString("message"));
            }else{
                Utils.traces("Response Set pair token ptt failed");
            }

        }catch (Exception e){
            taskMessage.setMessage("Set pair token ptt failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
           clearInstances();
        }

        return taskMessage;
    }

    public TaskMessage requestToken(long idGroup, String username) {
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.setApiEnum(ApiEnum.REQUEST_TOKEN);
        params = new ArrayList<>();
        httpClient = new DefaultHttpClient();

        try {
            url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/gettoken?imei='" + Conf.DEVICE_TOKEN + "'&groupId=" +idGroup + "&API_KEY='"+ Conf.API_KEY +"'&clientName='" + username + "'&username='" + username+"'";
            httpPost = new HttpPost(url);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = httpClient.execute(httpPost);
            respEntity = response.getEntity();

            if (respEntity != null) {
                taskMessage.setMessage(EntityUtils.toString(respEntity));
            }

        }catch (Exception e){
            taskMessage.setMessage("Set pair token ptt failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
           clearInstances();
        }

        return taskMessage;
    }

    public TaskMessage leaveToken(long idGroup, String username) {
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.setApiEnum(ApiEnum.LEAVE_TOKEN);
        params = new ArrayList<>();
        httpClient = new DefaultHttpClient();
        try {

            url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/releasetoken?imei='" + Conf.DEVICE_TOKEN + "'&groupId=" + idGroup + "&API_KEY='" + Conf.API_KEY + "'&clientName='" + username + "'&username='" + username+"'";

            httpPost = new HttpPost(url);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = httpClient.execute(httpPost);
            respEntity = response.getEntity();

            if (respEntity != null) {
                taskMessage.setMessage(EntityUtils.toString(respEntity));
            } else {
                taskMessage.setError(true);
                taskMessage.setMessage("Leave token failed");
            }
        }catch (Exception e){
            taskMessage.setMessage("Leave token failed");
            taskMessage.setError(true);
            taskMessage.setException(e);
        }finally {
          clearInstances();
        }

        return taskMessage;
    }

}
