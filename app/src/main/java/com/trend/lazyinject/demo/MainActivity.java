package com.trend.lazyinject.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.demo.component.TestComponent;
import com.trend.lazyinject.demo.component.TestComponentB;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.ModelA;
import com.trend.lazyinject.demo.model.NullProtectTestA;
import com.trend.lazyinject.demo.model.RemoteCallback;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Inject(component = TestComponent.class)
    public static List<Integer> integers;
    @InjectComponent
    public static TestComponent testComponent;
    @Inject(component = TestComponent.class)
    List<String> strings;
    @Inject(component = TestComponent.class, args = {"test"}, alwaysRefresh = true)
    BaseModel baseModel;

    @Inject(component = TestComponent.class)
    Map<String, ? extends Collection> map;

    //NullProtect
    @InjectComponent(nullProtect = true)
    NullProtectTestA nullProtectTestA;

    @Inject(nullProtect = true)
    NullProtectTestA nullProtectTestA2;

    @InjectComponent(nullProtect = true)
    NullProtectTestA nullProtectTestA3;

    Button btnInject;


    @Inject(component = TestComponent.class)
    Bundle bundle;

    ModelA ma = new ModelA();

    BaseModel ba = new BaseModel();

    @Inject(component = TestComponentB.class)
    Context context;

    @Inject(component = TestComponentB.class)
    static TestComponentB.Inner inner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnInject = findViewById(R.id.btn_inject);
        btnInject.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {


        try {
            ba.modelA.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


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
            Log.d("test", "BaseModel inject success = " + baseModel.toString());
        }
        if (map != null) {
            Log.d("test", "Map<String,ModelA> inject success = " + map.toString());
        }
        if (bundle != null) {
            Log.d("test", "Bundle inject success = " + bundle.toString());
        }

        if (context != null) {
            Log.d("test", "Context inject success = " + context.toString());
        }

        if (inner != null) {
            Log.d("test", "Inner inject success = " + inner.toString());
        }
        if (nullProtectTestA != null) {
            nullProtectTestA.test1();
            nullProtectTestA.test2("....");
        }
        if (nullProtectTestA2 != null) {
            nullProtectTestA2.test1();
            nullProtectTestA2.test2("....");
        }

        try {
            ma.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        nullProtectTestA3.test1();

        Log.d("test", "BaseModel invoke success = " + testComponent.provide4(null, null));
        Log.d("test", "Parcel invoke success = " + testComponent.invokeTestForParcel("a", new ModelA("a"), new Bundle(), new RemoteCallback().asBinder()));
    }
}
