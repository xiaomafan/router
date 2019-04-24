package com.xiaoma.router;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.xiaoma.mylibrary.RouterService;
import com.xiaoma.mylibrary.User;
import com.xiaoma.routerapi.Router;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_jump)
    Button mBtnJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_jump)
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_jump:
                annotationStartActivity();
                break;
        }
    }

    private void annotationStartActivity() {
        RouterService routerService = new Router(this).create(RouterService.class);
        User user = new User("张三来了", 300);
        routerService.startLibraryActivity("xiaoma", user);
    }
}
