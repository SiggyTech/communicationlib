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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class NetworkConnection {

    private DaoSession mDaoSession;

    public  String register(Long id, String name, String API_KEY, int groupId, String ip, int port){
        try {
            String url_select = Conf.UR_CONTENT + "destination/register/" + id + "/" + name + "/" + ip + "/" + port + "/" + groupId + "/" + API_KEY;
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



    public String getDestList(String imei, Context context, int igGroup, String API_KEY){

        String url_select =Conf.SERVER_IP + "/destination/get/" + imei + "/" + igGroup + "/" + API_KEY;

        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

        try {
            mDaoSession = new DaoMaster(
                    new DaoMaster.DevOpenHelper(context, "ptt_content.db").getWritableDb()).newSession();

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url_select);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log
            final JSONObject jObject = new JSONObject(EntityUtils.toString(httpEntity));

            //delete all by de testing process
            mDaoSession.getDestinationDao().deleteAll();

            for(int j = 0; j < jObject.getJSONArray("Destinations").length(); j++)
            {
                JSONObject n =  jObject.getJSONArray("Destinations").getJSONObject(j);

                mDaoSession.getDestinationDao().insert(new Destination(Long.parseLong(n.get("id").toString()),
                        n.get("name").toString(),
                        n.get("ip").toString(),
                        Integer.parseInt(n.get("port").toString()),
                        Integer.parseInt(n.get("idgroup").toString())
                    )
                );

                Log.d("EEE", "Level " + n.get("idLevel").toString());

            }
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingEx", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "OK";
    }
}
