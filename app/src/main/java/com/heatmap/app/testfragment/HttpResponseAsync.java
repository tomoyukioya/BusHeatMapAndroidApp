package com.heatmap.app.testfragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

/**
 * Created by Ohya on 2017/11/01.
 */
// https://qiita.com/a_nishimura/items/19cf3f60ad1dd3f66a84

public class HttpResponseAsync extends AsyncTask<VisibleRegion, Void, List<HeatMapData>> {

    // メンバ変数
    private HttpResponseAsync.HttpResponseAsyncInterface mListener;

    // 初期化：Listner登録
    HttpResponseAsync(Fragment fragment){
        if (fragment instanceof HttpResponseAsync.HttpResponseAsyncInterface) {
            mListener = (HttpResponseAsync.HttpResponseAsyncInterface) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement HttpResponseAsyncInterface");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // doInBackground前処理
    }

    @Override
    protected List<HeatMapData> doInBackground(VisibleRegion... params) {
        HttpURLConnection con = null;
        String urlSt = "http://trafficmap.azurewebsites.net/api/values";

        try {
            // POSTするJSONデータの作成
            HeatMapPostdata data = new HeatMapPostdata();
            data.visibleRegion = params[0];
            data.maxPoint = 1000;

            Gson gson = new Gson();
            String requestJson = gson.toJson(data);

            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpURLConnection) new URL(urlSt).openConnection();
            // リクエストメソッドの設定
            con.setRequestMethod("POST");
            // リダイレクトを自動で許可しない設定
            con.setInstanceFollowRedirects(false);
            // URL接続からデータを読み取る場合はtrue
            con.setDoInput(true);
            // URL接続にデータを書き込む場合はtrue
            con.setDoOutput(true);

            con.setFixedLengthStreamingMode(requestJson.getBytes().length);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // 接続
            con.connect();

            DataOutputStream os = new DataOutputStream(con.getOutputStream());
            os.write(requestJson.getBytes("UTF-8"));
            os.flush();
            os.close();

            StringBuffer responseJson = new StringBuffer();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    responseJson.append(inputLine);
                }
            }

            // 結果のパース
            List<HeatMapData> result = gson.fromJson(responseJson.toString(),
                    $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, HeatMapData.class));

            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<HeatMapData> result) {
        super.onPostExecute(result);
        // doInBackground後処理
        mListener.OnHttpResponseReady(result);
    }

    public interface HttpResponseAsyncInterface {
        void OnHttpResponseReady(List<HeatMapData> result);
    }
}

class HeatMapPostdata
{
    VisibleRegion visibleRegion;
    int maxPoint;
}
