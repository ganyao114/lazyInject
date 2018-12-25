# 1.lazyInject
被动依赖注入框架 for Android

 [ ![Download](https://api.bintray.com/packages/ganyao114/maven/lib/images/download.svg) ](https://bintray.com/ganyao114/maven/lib/_latestVersion)
# 2.配置
## Gradle  
根目录/build.gradle  
```groovy
buildscript {
    
    ...
    
    dependencies {
        ...
        //此依赖用于实现 AspectJ,如有其他合适项目可以自行替换，现由 https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx 实现
        classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.1'
        ...
    }
}  
  
...  
  
allprojects {
    repositories {
        ...
        maven {
            url "https://dl.bintray.com/ganyao114/maven/"
        }
        ...
    }
}

```
app 或者 lib/build.gradle
```groovy
//同上：如有其他 AspectJ 实现请自行替换
apply plugin: 'android-aspectjx'

dependencies {
    compile 'com.trend.lazyinject:lib:1.0.0'
    annotationProcessor 'com.trend.lazyinject:compiler:1.0.0'
    //如果使用 kotlin
    compile 'com.trend.lazyinject:kotlinsupport:1.0.0'
}

```  
## 混淆  
```proguard
-keepattributes *Annotation*
#保留部分泛型信息，必要!
-keepattributes Signature
#手动启用support keep注解
#http://tools.android.com/tech-docs/support-annotations
-dontskipnonpubliclibraryclassmembers
-keep,allowobfuscation @interface android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {
*;
}

-keepclassmembers class * {
    @android.support.annotation.Keep *;
}
#手动启用Component注解
#http://tools.android.com/tech-docs/support-annotations
-keep,allowobfuscation @interface com.trend.lazyinject.annotation.Component

-keep,allowobfuscation @com.trend.lazyinject.annotation.Component class * {
*;
}

-keepclassmembers class ** {
    @com.trend.lazyinject.annotation.Provide <methods>;
}

-keepclassmembers class ** {
     @com.trend.lazyinject.annotation.Inject <fields>;
}

-keepclassmembers class ** {
     @com.trend.lazyinject.annotation.InjectComponent <fields>;
}

-dontwarn javassist.**
```  
# 3.Example  
## Component  
&nbsp;&nbsp;参考 Dagger2，在 LazyInject 中 Component 为注入容器。  
### Component 定义  
```java
@Component
public interface TestComponent {
    @Provide
    List<String> provide1();
    @Provide
    ArrayList<Integer> provide2();
    @Provide
    ArrayList<? extends BaseModel> provide3();
    @Provide(singleton = true)
    ModelA provide4();
    @Provide
    Map<String,BaseModel> provide5();
    @Provide
    Map<String, ? extends ModelA> provide6();
}
```
&nbsp;&nbsp;打上 @Provide 注解的方法将被暴露为依赖的提供者，每个 provide 方法可单独配置为 singleton
### Component 实现
```java
@ComponentImpl
public class TestComponentImpl implements TestComponent {
    @Override
    public List<String> provide1() {
        return new ArrayList<>();
    }
    @Override
    public ArrayList<Integer> provide2() {
        return new ArrayList<>();
    }
    @Override
    public ArrayList<? extends BaseModel> provide3() {
        return new ArrayList<>();
    }
    @Override
    public ModelA provide4() {
        return new ModelA();
    }
    @Override
    public Map<String, BaseModel> provide5() {
        return new HashMap<>();
    }
    @Override
    public Map<String, ? extends ModelA> provide6() {
        return new HashMap<>();
    }
}
```
```java@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface ComponentImpl {
    String component() default "";
    String name() default "default";
    boolean cache() default true;
}
```
需要注意两个参数:  
1. name 指定后，如果某个 Component 存在多个实现类，可在编译时指定具体的实现。  
2. cache = false 时，每次注入都将会调用下面所说的 build 静态方法(也就是说默认会 new 一个新的 Component)，一般适用于当 Component 为 MVP 中的 Presenter 时。
### Component 管理
&nbsp;&nbsp;Component 默认在进程中全局单例
####手动管理
```java
LazyInject.registerComponent(component, instance);
LazyInject.getComponent(component, instance);
LazyInject.removeComponent(component, instance);
```
#### 自动管理
##### 编写 BuildMap
&nbsp;&nbsp;为了让框架找到对应 Component 的构造方法，你需要实现一个完全由对应静态方法构成的类，该类默认会由注解处理器自动生成
```java
@Keep
public class Auto_ComponentBuildMap {
  public static TestComponent buildTestComponentImpl() {
    return new com.trend.lazyinject.demo.component.TestComponentImpl();
  }
}
```
&nbsp;&nbsp;需要做的就是在 Application 初始化时调用
```java
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LazyInject.addBuildMap(Auto_ComponentBuildMap.class);
    }
}
```
&nbsp;&nbsp;当然也可以模仿这个手动编写
## 注入
### 被动注入
&nbsp;&nbsp;被动注入的原理是利用 AspectJ 编译时 hook field get 操作。所以注入是被动的。  
&nbsp;&nbsp;除了加上 @Inject 注解并不需要做其他操作。
#### @Inject
```java
@Target(FIELD)
@Retention(RUNTIME)
public @interface Inject {
    //定义该注入对象所在的 Component 容器类型,默认取注入类型的外部类。
    Class<?> component() default None.class;
    //为 true 时每次 get 该 field 都会注入一个新的值
    boolean alwaysRefresh() default false;
    //可以向注入方法传递 String 类型的参数
    String[] args() default {};  
    //当无法找到合适的值时,会自动虚拟一个对象以避免空指针异常。  
    boolean nullProtect() default false;  

    class None {}

}
```
//定义该注入对象所在的 Component 容器类型,默认取注入类型的外部类。  
Class<?> component() default None.class;  
//为 true 时每次 get 该 field 都会注入一个新的值  
boolean alwaysRefresh() default false;  
//可以向注入方法传递 String 类型的参数  
String[] args() default {};  
//当无法找到合适的值时,会自动虚拟一个对象以避免空指针异常。 
boolean nullProtect() default false;  

Example
```java
public class MainActivity extends AppCompatActivity {

    @Inject(component = TestComponent.class)
    List<Integer> integers;
    @InjectComponent
    TestComponent testComponent;
    @Inject(component = TestComponent.class)
    List<String> strings;
    @Inject(component = TestComponent.class, alwaysRefresh = true)
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
```
#### @InjectComponent
&nbsp;&nbsp;@InjectComponent 注解用于注入 Component 容器。  

&nbsp;&nbsp;一般使用 MVP 时常用到。
```java
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
```

```java
//Presenter 不 cache
@ComponentImpl(cache = false, name = "Product")
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
```

```java
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
```
### 主动注入
&nbsp;&nbsp;主动注入不需要开启 AspectJ  
&nbsp;&nbsp;区别在于需要用户手动调用 LazyInject.inject(this);  
&nbsp;&nbsp;不支持 alwaysRefresh

### Provider 方法参数依赖注入
&nbsp;&nbsp;类似 Dagger Provider 方法可以带待注入的参数
1. 如果不在参数列表上加 Inject 注解，则默认会在本模块中搜索合适的依赖
2. 加上 Inject 或者 InjectComponent 则会注入对应模块的依赖，参考上面 Field 注入写法
3. 搜索不到则为 Null，String 类型参数可从注解传入

```java
    @Provide
    ModelA provide4(Map<String,BaseModel> strings,String test ,@InjectComponent TestComponent testComponent);
```


### Kotlin Support
#### 使用 Kotlin 特性动态代理
```kotlin
val map: Map<Any,Any> by provideElement(TestComponent::class)
```
&nbsp;&nbsp;缺点在于不能混淆,混淆会丢失类型元数据，请等待 proguard 修复，从设计角度来说 kotlin side 难以修复。  
&nbsp;&nbsp;case:https://youtrack.jetbrains.com/issue/KT-21869  
#### 继续使用注解，和 Java 类似
```kotlin
@Provide(component = TestComponent::class, alwaysRefresh = true)
var strs: List<String>? = null;
```
## Build 配置
&nbsp;&nbsp;对应子 Module/build.gradle
```groovy
android {
    ...
    defaultConfig {
        ...  
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ targetPackage : 'com.trend.lazyinject.demo' , targetClassName : 'AppBuildMap', defaultComponent : 'Product']
            }
        }

    }
}
```
&nbsp;&nbsp;自动生成 com.trend.lazyinject.demo.AppBuildMap，并且选用 Product 实现  

## Debug 开关
&nbsp;&nbsp;打开将显示 log  
```java
LazyInject.setDebug(true);
```

## 实验性功能
### 在子进程实现 Component
依赖 2.0.3-beta 版本可以使用此 Feature
代码在 multi_process 分支
```java
@ComponentImpl(process = "com.trend.lazyinject.demo.p1")
public class TestComponentImpl implements TestComponent {
    ...
}
```
需要在 Manifest 中为子进程注册一个 StubProvider, authorities 必须与子进程名相同。注意不要 exported, 否则会有安全风险
```xml
<provider
      android:authorities="com.trend.lazyinject.demo.p1"
      android:name="com.trend.lazyinject.lib.ipc.InjectIPCProvider"
      android:process="com.trend.lazyinject.demo.p1" />
```
因为需要 IPC，所以所有 Provider 方法的参数和返回值必须继承自 Serializable/Parcelable/IBinder
你也可以直接调用 Component 中的方法，这些都是支持 IPC 的
# 实现原理  
[Design Document](https://github.com/ganyao114/lazyInject/blob/master/doc/di_design.md)

# Email
939543405@qq.com  
