package com.xiaoma.mylibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.xiaoma.annotation.inject.Inject;
import com.xiaoma.routerapi.RouterInject;

public class LibraryActivity extends AppCompatActivity {

    /**
     * 数据
     */
    private Button mBtnClick;
    /**
     * My Library
     */
    private TextView mTvContent;

    @Inject
    User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        RouterInject.inject(this);
//        new LibraryActivity_RouterInjecting<>(this);
        initView();
    }

    private void initView() {
        mBtnClick = (Button) findViewById(R.id.btn_click);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        String stringParam = getIntent().getStringExtra("stringParam");
//        User user = getIntent().getParcelableExtra("user");
        mTvContent.setText(stringParam + "/libray//" + user.toString());
    }

}
