package com.trend.lazyinject.demo.mvp;

import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.Provide;

import java.io.Serializable;

/**
 * Created by swift_gan on 2018/4/17.
 */

public interface LoginMVP {
    interface View {
        void loginSuccess();
        void loginError();
    }
    @Component
    interface Presenter {
        void attachView(View view);
        void login(String name, String pass);
        void dettachView();
        @Provide
        User loginedUser();
    }
    interface UserManager {
        User getUser();
        void setUser(User user);
        void logout();
    }
    class User implements Serializable {
        public String name;
        public String sessionId;
    }
}
