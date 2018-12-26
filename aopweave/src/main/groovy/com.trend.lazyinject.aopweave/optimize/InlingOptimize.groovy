package com.trend.lazyinject.aopweave.optimize

import com.trend.lazyinject.aopweave.annotation.AnnotationParser
import com.trend.lazyinject.aopweave.infos.InjectAnnoInfo
import com.trend.lazyinject.aopweave.provider.ProviderSeach
import javassist.ClassPool
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.expr.FieldAccess

import java.util.concurrent.ConcurrentHashMap

public class InlingOptimize {

    ClassPool classPool

    Map<CtField, CtMethod> methods = new ConcurrentHashMap<>()

    InlingOptimize(ClassPool classPool) {
        this.classPool = classPool
    }

    public boolean optimize(FieldAccess fieldAccess, boolean isInjectComponent) {
        CtField field = fieldAccess.field
        CtMethod method = getAndInjectOptimizedMethod(field, {
            CtMethod targetMethod = null
            if (isInjectComponent) {

            } else {
                InjectOptimizeInfo injectOptimizeInfo = getInjectOptimizeInfo(field)
                if (injectOptimizeInfo == null)
                    return null
                targetMethod = CtNewMethod.make(field.type, getMethodName(field), null, null, generateInjectOptimizedMethod(field, injectOptimizeInfo), field.declaringClass)
            }
            if (targetMethod != null) {
                field.declaringClass.addMethod(targetMethod)
            }
            return targetMethod
        })
        if (method == null)
            return false
        fieldAccess.replace("\$_ = \$0.${method.name}();")
        return true
    }

    public CtMethod getAndInjectOptimizedMethod(CtField field, MakeMethodCallback callback) {
        CtMethod ctMethod = methods.get(field)
        if (ctMethod != null)
            return ctMethod
        synchronized (field) {
            ctMethod = methods.get(field)
            if (ctMethod != null)
                return ctMethod
            try {
                ctMethod = field.declaringClass.getDeclaredMethod(getMethodName(field), null)
            } catch (Exception e) {}
            //此处最好检查方法注解信息，保证增量更新
            if (ctMethod == null) {
                ctMethod = callback.makeMethod()
            }
            if (ctMethod != null) {
                methods.put(field, ctMethod)
            }
            return ctMethod
        }
    }

    public String generateInjectOptimizedMethod(CtField field, InjectOptimizeInfo optimizeInfo) {

        String fieldName = "this." + field.name
        String componentType = optimizeInfo.annoInfo.component.name
        String providerName = optimizeInfo.providerMethod.name
        String lock = "\"" + "INJECT_LOCK-" + field.declaringClass.name + "." + "field.name" + "@" + " + hashCode()" + "\""

        if (optimizeInfo.annoInfo.alwaysRefresh) {
            return " {\n" +
                    "        ${componentType} component = com.trend.lazyinject.lib.LazyInject.getComponent(${componentType}.class);\n" +
                    "        if (component == null)\n" +
                    "            return null;\n" +
                    "        ${fieldName} = component.${providerName}();\n" +
                    "        return ${fieldName};\n" +
                    "    }"
        } else {
            return "{\n" +
                    "        if (${fieldName} != null) {\n" +
                    "            return ${fieldName};\n" +
                    "        } else {\n" +
                    "            synchronized (${lock}.intern()) {\n" +
                    "                ${componentType} component = com.trend.lazyinject.lib.LazyInject.getComponent(${componentType}.class);\n" +
                    "                if (component == null)\n" +
                    "                    return null;\n" +
                    "                ${fieldName} = component.${providerName}();\n" +
                    "                return ${fieldName};\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }"
        }
    }

    public String generateInjectComponentOptimizedMethod(CtField field) {


        String fieldName = field.name


        String body = "{" +
                "" +
                "" +
                "" +
                "" +
                "" +
                "" +
                "}"
    }

    public static String getMethodName(CtField field) {
        return "lazyInject_get_${field.getName()}"
    }

    public static String isLzOptimizedSetter(CtMethod ctMethod) {
        return ctMethod.getName().startsWith("lazyInject_get_")
    }

    class InjectOptimizeInfo {

        InjectAnnoInfo annoInfo
        CtMethod providerMethod

        InjectOptimizeInfo(InjectAnnoInfo annoInfo, CtMethod providerMethod) {
            this.annoInfo = annoInfo
            this.providerMethod = providerMethod
        }
    }

    InjectOptimizeInfo getInjectOptimizeInfo(CtField field) {
        InjectAnnoInfo annoInfo = AnnotationParser.getInjectInfo(field, classPool)
        if (annoInfo == null)
            return null
        if (annoInfo.nullProtect)
            return null
        if (annoInfo.args != "null")
            return null
        CtMethod provider = ProviderSeach.search(field, annoInfo.component)
        if (provider == null)
            return null
        return new InjectOptimizeInfo(annoInfo, provider)
    }

    interface MakeMethodCallback {
        CtMethod makeMethod()
    }

}
