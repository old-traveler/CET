package com.cet.http;

import com.cet.bean.Result;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by hyc on 2018/4/19 21:10
 */

public interface Api {

    @GET("api")
    Call<Result> loadResult(@QueryMap Map<String, String> params);


}
