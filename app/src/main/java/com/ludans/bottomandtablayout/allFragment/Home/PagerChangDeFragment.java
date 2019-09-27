package com.ludans.bottomandtablayout.allFragment.Home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ludans.bottomandtablayout.R;
import com.ludans.bottomandtablayout.allAdapter.MyRecyclerViewAdapter;
import com.ludans.bottomandtablayout.allBean.ChangDeNewsBean;
import com.ludans.bottomandtablayout.allFragment.PagerFragment;
import com.ludans.bottomandtablayout.utils.OkHttpsUtils;
import com.ludans.bottomandtablayout.utils.RandomPath;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class PagerChangDeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int mParam1;
    private String mParam2;

    private static final String TAG = "PagerChangDeFragment";


    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private List<ChangDeNewsBean> mDate;
    private MyRecyclerViewAdapter adapter;
    private RandomPath randomPath ;


    public PagerChangDeFragment() {
    }

    public static PagerChangDeFragment newInstance(int param1, String param2) {
        PagerChangDeFragment fragment = new PagerChangDeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);

        initView(rootView,1);
        return rootView;
    }

    public void initView(View rootView, int typeUrl) {

        //设置RecyclerView适配器
        mDate = new ArrayList<>();
        TextView textView = rootView.findViewById(R.id.pager_text);
        // RecyclerView 初始化
        recyclerView = rootView.findViewById(R.id.pager_recycler);
        // swipeRefreshLayout 初始化
        swipeRefreshLayout = rootView.findViewById(R.id.pager_refresh);
        swipeRefreshLayout.setRefreshing(true);

//        network by OkHttp frame to get json data
        creatThread(typeUrl);
//        Setting RecyclerView and Bind adapter
        //联网请求
        //数据刷新
        seeRefresh(typeUrl);
    }

    public void creatThread(final int urlType) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                postJson( urlType);
            }
        }.start();
    }


    public void postJson(int type) {
        randomPath = new RandomPath(type);
        String homePath = randomPath.getPath();
        Log.d(TAG, "网站："+homePath);
        Log.d(TAG, "postJson: 随机url：" + homePath);

        OkHttpsUtils.sendRequestWithOkHttp(homePath, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: 网络连接失败！");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "onResponse: 网络连接成功！！");
                String body = response.body().string();
                Gson gson = new Gson();
                mDate = gson.fromJson(body, new TypeToken<ArrayList<ChangDeNewsBean>>() {
                }.getType());
                Log.d(TAG, "onResponse: Gson 解析成功！");
                adapter = new MyRecyclerViewAdapter(getContext(), (ArrayList<ChangDeNewsBean>) mDate);

//            Setting Adapter and Setting mData

                Message message = new Message();
                message.obj = body;
                handler.sendMessage(message);
            }
        });
    }

    public void seeRefresh(final int typeUrl) {
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        //监听刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                creatThread(typeUrl);
            }
        });
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String responseBody = (String) msg.obj;
            Gson gson = new Gson();
            mDate = gson.fromJson(responseBody, new TypeToken<ArrayList<ChangDeNewsBean>>() {
            }.getType());
            Log.d(TAG, "onResponse: Gson 解析成功！");
            adapter = new MyRecyclerViewAdapter(getContext(), (ArrayList<ChangDeNewsBean>) mDate);

//            Setting Adapter and Setting mData


            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);

        }
    };

}
