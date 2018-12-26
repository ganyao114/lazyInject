package com.trend.lazyinject.aopweave.provider

import com.trend.lazyinject.annotation.Provide
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod

public class ProviderSeach {
    public static CtMethod search(CtField field, CtClass component) {
        CtMethod[] methods = component.getMethods()
        if (methods == null || methods.length == 0)
            return null
        for (CtMethod method:methods) {
            Provide provide = method.getAnnotation(Provide.class)
            if (provide == null || provide.singleton())
                continue
            if (method.returnType == null)
                return null
            CtClass[] pars = method.getParameterTypes()
            if (pars == null || pars.length == 0) {
                if (canCast(field, method))
                    return method
            }
        }
    }

    //最好使用严格模式
    public static boolean canCast(CtField field, CtMethod method) {
        String fieldGeneric = field.genericSignature
        String providerGeneric = method.genericSignature
        CtClass fieldType = field.type
        CtClass providerType = method.returnType
        //接受者无范型，则只需关注接受者和提供者的 Class
        if (fieldGeneric == null || fieldGeneric.length() == 0) {
            return canCastRaw(providerType, fieldType)
        } else {
            if (providerGeneric == null || providerGeneric.length() == 0) {
                return false
            } else {
                return providerGeneric.equals("()" + fieldGeneric)
            }
        }
    }

    public static boolean canCastRaw(CtClass child, CtClass parent) {
        while (child != null && child.name != "java.lang.Object") {
            try {
                if (child == parent || child.name == parent.name || isSonOfInstance(child, parent))
                    return true
                child = child.getSuperclass()
            } catch (Exception e) {
                break
            }
        }
        return false
    }

    public static boolean isSonOfInstance(CtClass child, CtClass parent) {
        CtClass[] interfaces = child.getInterfaces()
        if (interfaces == null || interfaces.length == 0)
            return false
        for (CtClass inter:interfaces) {
            if (child == parent || child.name == parent.name) {
                return true
            } else {
                if (isSonOfInstance(inter, parent))
                    return true
            }
        }
        return false
    }
}
