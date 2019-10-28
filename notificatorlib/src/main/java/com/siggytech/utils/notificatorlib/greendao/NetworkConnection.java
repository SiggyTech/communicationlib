package com.siggytech.utils.notificatorlib.greendao;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.util.Log;
import com.siggytech.utils.notificatorlib.Conf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class NetworkConnection {

    public  String register(Long id, String name, String API_KEY, int groupId, String ip, int port){
        try {
            String url_select = Conf.SERVER_IP + "/destination/register/" + id + "/" + name + "/" + ip + "/" + port + "/" + groupId + "/" + API_KEY;
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url_select);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log

            final JSONObject jObject = new JSONObject(EntityUtils.toString(httpEntity));
            JSONObject n = jObject.getJSONObject("Result");
            Log.e("register: ", n.get("result").toString());
            return n.get("result").toString();
        }
        catch(Exception ex){
            Log.e("registerException", ex.toString());
            return "Error: " + ex.toString();

        }

    }




}
