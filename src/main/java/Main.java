import com.github.axet.wget.WGet;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Main {
    public static final String DOWNLOAD_URL = "http://api.kwai.com/rest/o/feed/hot?language=en-gb&appver=1.2.0.501507&c=GOOGLE_PLAY&oc=GOOGLE_PLAY&ud=1064631422&app=1&did=ANDROID_b1fd4570b17c9a01&lat=0&lon=0&mod=HUAWEI%28WAS-TL10%29&net=WIFI&sys=KWAI_ANDROID_8.0.0&ver=1.2&iuid=&country_code=cn";
    public static final int JSON_COUNT = 10; // 启动一次遍历10次网址 每次10个 总共100个
    public static final String ROOT_PATH = "F://kwaiMvDownload//";
    public static final int TIME_INTERVAL = 60; // 每隔一定时间执行一次，单位分钟

    private static String[] mRefreshTime = {"1", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private static String[] mPcursor = {"", "1", "1", "1", "1", "1", "1", "1", "1", "1"};
    private static String[] mSig = {"8847b7c5098ecc87dbf38a8bfb15406f"
            , "2b21e1924a1482ff3dd3e9c06557d424"
            , "874597b0e3869005aa1483da658c6b61"
            , "76822a42b433b1806a5c38b5f68900ae"
            , "95503a4b208ebc691a505cd724d8a992"
            , "b286fda39efd775d48c75e19469715fc"
            , "0a57fe4d91923cd9032df5a79034f06f"
            , "6eae2285c3338ee4f185ea62e68e11d4"
            , "44738a982ad1df2c6ef82beb340368e4"
            , "279bcaae5dc318c1298ba686742c5195"};

    private static String[] mNStokensig = {"72fa997a8c9d32c06fbeebf16bce5594d3c9133ebe67e71124f759fee8f6d9ed"
            , "fd0e14a07f5c5c3cd1e3d78c1516369091d91367270b0b55e7bddf342785eed4"
            , "061f0a8ccb818c49f90409738fbbb2f2ce04450e35188852efb6caae7946f6c0"
            , "6ec7a229941c8d62f3aa63ef45b463987707fabcf8e658b71aedc2ba412fb902"
            , "c960c2e14310a64b02f7538d5429aa380bf7792a5bf9f13488384dc312013f68"
            , "1823cc1e960acc01602e949f93167041cab46c2253faf5dfe3d78c79952fea2a"
            , "56913c2bc00f51a3191420fa31ded41b41069257f8cf967e95aa2fd9acb511be"
            , "12b6675d72a39d69a49ff98ffc85bec9bb3da1dc9c5780e183c5dcc03cb0b89c"
            , "1a802f52706021c77759a81f646b6b055ffd6d3da77f26b906ee69b2c633e6e3"
            , "09f81d7b63df1e223a40e6b0294096f23132a0b52f07565b55288d95a462eb87"};

    private static Timer mTimer;

    public static void main(String[] args) {
        mTimer = new Timer();
        mTimer.schedule(new TimerDownLoad(ROOT_PATH), 0, TimeUnit.MINUTES.toMillis(TIME_INTERVAL));
    }

    private static class TimerDownLoad extends TimerTask {

        private String filePath;

        public TimerDownLoad(String path) {
            filePath = path;
        }

        public void run() {
            List<String> kwaiJsons = getKwaiJsons();
            List<String> mvURLs = new ArrayList<String>();
            Gson gson = new Gson();
            for (String kwaiJson : kwaiJsons) {
                KwaiBean kwaiBean = gson.fromJson(kwaiJson, KwaiBean.class);
                List<KwaiBean.FeedsBean> feeds = kwaiBean.getFeeds();
                for (KwaiBean.FeedsBean feed : feeds) {
                    List<KwaiBean.FeedsBean.MainMvUrlsBean> main_mv_urls = feed.getMain_mv_urls();
                    for (KwaiBean.FeedsBean.MainMvUrlsBean main_mv_url : main_mv_urls) {
                        mvURLs.add(main_mv_url.getUrl());
                    }
                }
            }

            for (String mvURL : mvURLs) {
                System.out.println(mvURL);
                try {
                    URL url = new URL(mvURL);
                    File target = new File(filePath);
                    WGet w = new WGet(url, target);
                    w.download();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        private List<String> getKwaiJsons() {
            String kwaiJson = "";
            List<String> kwaiJsons = new ArrayList<String>();
            try {
                System.setProperty("http.proxyHost", "127.0.0.1");
                System.setProperty("http.proxyPort", "1080");

                URL url = new URL(DOWNLOAD_URL);
                OkHttpClient client = new OkHttpClient();

                for (int i = 0; i < JSON_COUNT; i++) {
                    FormBody body = new FormBody.Builder()
                            .add("page", "1")
                            .add("coldStart", "false")
                            .add("count", "10")
                            .add("pv", "false")
                            .add("refreshTimes", mRefreshTime[i])
                            .add("pcursor", mPcursor[i])
                            .add("os", "android")
                            .add("sig", mSig[i])
                            .add("token", "cb11091d582243e3a1b742403b54df79-1064631422")
                            .add("client_key", "3c2cd3f3")
                            .add("__NStokensig", mNStokensig[i])
                            .build();

                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .addHeader("User-Agent", "kwai-android")
                            .build();

                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                        kwaiJson = response.body().string();
                        kwaiJsons.add(kwaiJson);

                        System.out.println(kwaiJson);
                        System.out.println("***********    " + i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return kwaiJsons;
        }
    }


}
