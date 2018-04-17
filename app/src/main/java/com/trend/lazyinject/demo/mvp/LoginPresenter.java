package com.trend.lazyinject.demo.mvp;

import com.trend.lazyinject.annotation.ComponentImpl;

/**
 * Created by swift_gan on 2018/4/17.
 */
//Presenter 不 cache
@ComponentImpl(cache = false)
public class LoginPresenter implements LoginMVP.Presenter {
    LoginMVP.User user = new LoginMVP.User();
    @Override
    public void attachView(LoginMVP.View view) {}

    @Override
    public void login(String name, String pass) {}
    @Override
    public void dettachView() {}

    @Override
    public LoginMVP.User loginedUser() {
        return user;
    }
}
