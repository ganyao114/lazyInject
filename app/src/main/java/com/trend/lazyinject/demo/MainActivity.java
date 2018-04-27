package com.trend.lazyinject.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.demo.component.TestComponent;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.NullProtectTestA;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Inject(component = TestComponent.class)
    public static List<Integer> integers;
    @InjectComponent
    public static TestComponent testComponent;
    @Inject(component = TestComponent.class)
    List<String> strings;
    @Inject(component = TestComponent.class)
    BaseModel baseModel;
    @Inject(component = TestComponent.class)
    Map<String, ? extends Collection> map;

    //NullProtect
    @InjectComponent(nullProtect = true)
    NullProtectTestA nullProtectTestA;
    @Inject(nullProtect = true)
    NullProtectTestA nullProtectTestA2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (integers != null) {
            Log.d("test", "List<Integer> inject success = " + integers.toString());
        }
        if (testComponent != null) {
            Log.d("test", "TestComponent inject success = " + testComponent.toString());
        }
        if (strings != null) {
            Log.d("test", "List<String> inject success = " + strings.toString());
        }
        if (baseModel != null) {
            Log.d("test", "BaseModel inject success = " + baseModel.toString());
        }
        if (map != null) {
            Log.d("test", "Map<String,ModelA> inject success = " + map.toString());
        }
        if (nullProtectTestA != null) {
            nullProtectTestA.test1();
            nullProtectTestA.test2("....");
        }
        if (nullProtectTestA2 != null) {
            nullProtectTestA2.test1();
            nullProtectTestA2.test2("....");
        }
    }
}
