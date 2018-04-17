package com.trend.lazyinject.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.demo.component.TestComponent;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.ModelA;
import com.trend.lazyinject.demo.mvp.LoginMVP;
import com.trend.lazyinject.lib.LazyInject;
import com.trend.lazyinject.lib.log.LOG;

import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements LoginMVP.View {
    @InjectComponent
    LoginMVP.Presenter loginPresenter;
    @Inject
    LoginMVP.User lastUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginPresenter.attachView(this);
        //注入 lastUser
        LazyInject.inject(this, loginPresenter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.dettachView();
    }
    @Override
    public void loginSuccess() {}
    @Override
    public void loginError() {}
}
