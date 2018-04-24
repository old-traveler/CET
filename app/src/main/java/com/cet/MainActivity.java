package com.cet;

import android.media.MediaPlayer;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cet.adapter.RecordAdapter;
import com.cet.bean.Record;
import com.cet.bean.Result;
import com.cet.http.Api;
import com.cet.utils.CommonUtil;
import com.cet.utils.DbUtil;
import com.cet.utils.RetrofitUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MainActivity extends AppCompatActivity {

    Api server;

    TextView tv_result;

    Button btn_source;

    Button btn_result;

    EditText et_input;

    FrameLayout fl_speak;

    RecyclerView recyclerView;

    RecordAdapter recordAdapter;

    ProgressBar progressBar;

    ProgressBar pb_main;

    DrawerLayout dl_main;

    boolean isAdd = true;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = RetrofitUtil.getInstance().create(Api.class);
        initEvent();
    }


    private void initEvent() {
        et_input = findViewById(R.id.et_input);
        tv_result = findViewById(R.id.tv_result);
        btn_result = findViewById(R.id.btn_result);
        btn_source = findViewById(R.id.btn_source);
        fl_speak = findViewById(R.id.fl_speak);
        recyclerView = findViewById(R.id.rv);
        pb_main = findViewById(R.id.pb_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.pb);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        dl_main = findViewById(R.id.dl_main);
        recordAdapter = new RecordAdapter(null);
        recordAdapter.setListener(record -> {
            showResult(record,null);
            dl_main.closeDrawers();
        });
        recyclerView.setAdapter(recordAdapter);
        et_input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(et_input.getText().toString());
            }
            return false;
        });
        dl_main.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(View drawerView) {
                if (!isAdd)return;
                progressBar.setVisibility(View.VISIBLE);
                Observable.create(e -> e.onNext(DbUtil.getDbUtil().getAllRecord()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(records->loadAllRecord((List<Record>) records));
                isAdd = false;
            }
            @Override
            public void onDrawerClosed(View drawerView) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    private void loadAllRecord(List<Record> records){
        recordAdapter.setData(records);
        progressBar.setVisibility(View.GONE);
    }

    public void search(String str){
        Observable.create(e ->e.onNext(DbUtil.getDbUtil().queryRecord(str)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(record->showResult((Record) record,str));
    }

    private void loadData(String str){
        pb_main.setVisibility(View.VISIBLE);
        Call<Result> model = server.loadResult(CommonUtil.getParams(str));
        model.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if(result.getErrorCode().equals("0")){
                    tv_result.setText(resultToString(result));
                    saveRecord(result.getSpeakUrl(),result.getTSpeakUrl());
                    showSpeak(result.getSpeakUrl(),result
                            .getTSpeakUrl(),result.getQuery());
                }else {
                    tv_result.setText("查询出错"+result.getErrorCode());
                }
                pb_main.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                tv_result.setText(t.getMessage());
                pb_main.setVisibility(View.GONE);
            }
        });
    }

    private void showSpeak(String s1,String s2,String word){
        if(!TextUtils.isEmpty(s1)||!TextUtils.isEmpty(s2)){
            fl_speak.setVisibility(View.VISIBLE);
            btn_source.setOnClickListener(new SpeakClickListener(s1,word,false));
            btn_result.setOnClickListener(new SpeakClickListener(s2,word,true));
        }else {
            fl_speak.setVisibility(View.GONE);
        }
    }



    private void saveRecord(String speakUrl,String toSpeakUrl){
        isAdd = true;
        Record record = new Record();
        record.setWord(et_input.getText().toString());
        record.setTranslation(tv_result.getText().toString());
        record.setSpeakUrl(speakUrl);
        record.settSpeakUrl(toSpeakUrl);
        Observable<Record> observable = Observable.create(e->e.onNext(record));
        observable.map(record1 -> DbUtil.getDbUtil()
                .addRecord(record1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res-> System.out.print(res));
    }



    private void showResult(Record record,String word){
        if (!TextUtils.isEmpty(record.getWord())){
            et_input.setText(record.getWord());
            tv_result.setText(record.getTranslation());
            showSpeak(record.getSpeakUrl(),record
                    .gettSpeakUrl(),record.getWord());
        }else {
            loadData(word);
        }
    }

    private StringBuilder resultToString(Result result){
        StringBuilder res = new StringBuilder();
        res.append("translation：");
        for (String s : result.getTranslation()) {
            res.append(s+"\t");
        }
        res.append("\n");
        res.append("[网络]：\n");
        if (result.getWeb()!=null){
            for (Result.WebBean webBean : result.getWeb()) {
                res.append(webBean.getKey());
                res.append(" [");
                for (String s : webBean.getValue()) {
                    res.append(s+",");
                }
                if (res.lastIndexOf(",")!=-1){
                    res.delete(res.length()-1,res.length());
                }
                res.append("]\n");
            }
        }
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    class SpeakClickListener implements View.OnClickListener{

        private String url;

        private String word;

        private boolean isDowning = false;

        private boolean isTo = false;

        private boolean isPrepare  = false;

        public SpeakClickListener(String url,String word,boolean isTo){
            this.word = word;
            this.url = url;
            this.isTo = isTo;
        }

        @Override
        public void onClick(View view) {
            if (TextUtils.isEmpty(url))
                return;
            if(url.indexOf("@")==-1){
                down();
            }else {
                play();
            }

        }

        private void play(){
            if (isPrepare){
                return;
            }

            File file = new File(url.split("@")[0]);
            if (!file.exists()){
                url = url.split("@")[1];
                down();
                return;
            }
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(url.split("@")[0]);
                isPrepare = true;
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            isPrepare = false;

        }


        public void down(){
            if (!isDowning){
                isDowning = true;
                Request request = new Request.Builder().url(url).build();
                new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        Toast.makeText(MainActivity.this
                                , e.getMessage(), Toast.LENGTH_SHORT).show();
                        isDowning = false;
                    }

                    @Override
                    public void onResponse(okhttp3.Call call
                            , okhttp3.Response response) throws IOException {
                        deal(response);
                    }
                });
            }
        }

        private void deal(okhttp3.Response response) throws IOException{
            BufferedSink bufferedSink = null;
            try {
                File dest = new File(CommonUtil.FILEROOT
                        ,CommonUtil.encode(isTo?"to"+word:word));
                Sink sink = Okio.sink(dest);
                bufferedSink = Okio.buffer(sink);
                bufferedSink.writeAll(response.body().source());
                bufferedSink.close();
                url = dest.getAbsolutePath()+"@"+url;
                if(isTo){
                    DbUtil.getDbUtil().updateToSpeck(word,url);
                }else {
                    DbUtil.getDbUtil().updateSpeck(word,url);
                }
                play();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e
                        .getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if(bufferedSink != null){
                    bufferedSink.close();
                }
                isDowning = false;
            }
        }
    }
}
