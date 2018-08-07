// LazyInjectIPC.aidl
package com.trend.lazyinject.lib.ipc;

// Declare any non-default types here with import statements

public interface LazyInjectIPC {

    int OP_PROVIDE = 1;
    int OP_INVOKE = 2;
    String OP_PROVIDE_S = "provide";
    String OP_INVOKE_S = "invoke";

    String KEY_CTYPE = "CTYPE";
    String KEY_PKEY = "PKEY";
    String KEY_ARGS = "ARGS";
    String KEY_RET = "RET";

    Object remoteProvide(Class componentType, String providerKey, Object[] args);
    Object remoteInvoke(Class componentType, String providerKey, Object[] args);

}
