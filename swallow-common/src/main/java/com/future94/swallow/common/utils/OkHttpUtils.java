package com.future94.swallow.common.utils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
public class OkHttpUtils {

    private static final OkHttpUtils OK_HTTP_UTILS = new OkHttpUtils();

    private final OkHttpClient client;


    private OkHttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        client = builder.build();
    }

    public static OkHttpUtils getInstance() {
        return OK_HTTP_UTILS;
    }

    /**
     * Post string.
     *
     * @param url  the url
     * @param json the json
     * @return the string
     * @throws IOException the io exception
     */
    public String post(final String url, final String json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        ResponseBody responseBody = client.newCall(request).execute().body();
        return Objects.isNull(responseBody) ? "" : responseBody.string();
    }
}
