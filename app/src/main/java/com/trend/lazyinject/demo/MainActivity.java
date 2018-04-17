package com.trend.lazyinject.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.demo.component.TestComponent;
import com.trend.lazyinject.demo.component.TestComponentImpl;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.ModelA;
import com.trend.lazyinject.lib.LazyInject;
import com.trend.lazyinject.lib.log.LOG;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Inject(component = TestComponent.class)
    List<Integer> integers;
    @InjectComponent
    TestComponent testComponent;
    @Inject(component = TestComponent.class)
    List<String> strings;
    @Inject(component = TestComponent.class)
    BaseModel baseModel;
    @Inject(component = TestComponent.class)
    Map<String,ModelA> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (integers != null) {
            LOG.LOGD("test", "List<Integer> inject success = " + integers.toString());
        }
        if (testComponent != null) {
            LOG.LOGD("test", "TestComponent inject success = " + testComponent.toString());
        }
        if (strings != null) {
            LOG.LOGD("test", "List<String> inject success = " + strings.toString());
        }
        if (baseModel != null) {
            LOG.LOGD("test", "BaseModel inject success = " + baseModel.toString());
        }
        if (map != null) {
            LOG.LOGD("test", "Map<String,ModelA> inject success = " + map.toString());
        }
    }
}
