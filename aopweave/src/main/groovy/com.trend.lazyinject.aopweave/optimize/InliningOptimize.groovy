package com.trend.lazyinject.aopweave.optimize

import com.trend.lazyinject.aopweave.annotation.AnnotationParser
import com.trend.lazyinject.aopweave.infos.InjectAnnoInfo
import com.trend.lazyinject.aopweave.infos.InjectComponentAnnoInfo
import com.trend.lazyinject.aopweave.provider.ProviderSeach
import com.trend.lazyinject.aopweave.weave.ClassChangeListener
import javassist.ClassPool
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.Modifier
import javassist.expr.FieldAccess

import java.util.concurrent.ConcurrentHashMap

public class InliningOptimize {

    ClassPool classPool

    Map<CtField, CtMethod> methods = new ConcurrentHashMap<>()

    ClassChangeListener classChangeListener

    InliningOptimize(ClassPool classPool) {
        this.classPool = classPool
        setClassChangeListener(new ClassChangeListener() {
            @Override
            void onClassChanged(String className) {

            }
        })
    }

    void setClassChangeListener(ClassChangeListener classChangeListener) {
        this.classChangeListener = classChangeListener
    }

    public boolean optimize(FieldAccess fieldAccess, boolean isInjectComponent) {
        CtField field = fieldAccess.field
        CtMethod method = getAndWeaveOptimizedMethod(field, {
            CtMethod targetMethod = null
            if (isInjectComponent) {
                InjectComponentOptimizeInfo injectComponentOptimizeInfo = getInjectComponentOptimizeInfo(field)
                if (injectComponentOptimizeInfo == null)
                    return null
                targetMethod = CtNewMethod.make(field.type, getMethodName(field), null, null, generateInjectComponentOptimizedMethod(field, injectComponentOptimizeInfo), field.declaringClass)
            } else {
                InjectOptimizeInfo injectOptimizeInfo = getInjectOptimizeInfo(field)
                if (injectOptimizeInfo == null)
                    return null
                targetMethod = CtNewMethod.make(field.type, getMethodName(field), null, null, generateInjectOptimizedMethod(field, injectOptimizeInfo), field.declaringClass)
            }
            if (targetMethod != null) {
                if (Modifier.isStatic(field.getModifiers())) {
                    targetMethod.setModifiers(Modifier.STATIC | Modifier.PUBLIC)
                }
                field.declaringClass.addMethod(targetMethod)
                classChangeListener.onClassChanged(field.declaringClass.name)
            }
            return targetMethod
        })
        if (method == null)
            return false
        fieldAccess.replace("\$_ = \$0.${method.name}();")
        classChangeListener.onClassChanged(fieldAccess.where().declaringClass.name)
        return true
    }

    public CtMethod getAndWeaveOptimizedMethod(CtField field, MakeMethodCallback callback) {
        CtMethod ctMethod = methods.get(field)
        if (ctMethod != null)
            return ctMethod
        synchronized (field) {
            ctMethod = methods.get(field)
            if (ctMethod != null)
                return ctMethod
            try {
                ctMethod = field.declaringClass.getDeclaredMethod(getMethodName(field), null)
                //remove old method
                if (ctMethod != null && !ctMethod.declaringClass.isFrozen()) {
                    field.declaringClass.removeMethod(ctMethod)
                    ctMethod = null
                }
            } catch (Exception e) {}
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

        String fieldName
        String lock

        if (Modifier.isStatic(field.getModifiers())) {
            fieldName = field.declaringClass.name + "." + field.name
            lock = "\"" + "LZ_LOCK_" + field.declaringClass.name + "." + field.name + "\""
        } else {
            fieldName = "this." + field.name
            lock = "(\"" + "LZ_LOCK_" + field.name + "@" + "\"" + " + hashCode())"
        }

        String componentType = optimizeInfo.annoInfo.component.name
        String providerName = optimizeInfo.providerMethod.name

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

    public String generateInjectComponentOptimizedMethod(CtField field, InjectComponentOptimizeInfo optimizeInfo) {

        String fieldName

        if (Modifier.isStatic(field.getModifiers())) {
            fieldName = field.declaringClass.name + "." + field.name
        } else {
            fieldName = "this." + field.name
        }

        String componentType = field.type.name

        if (optimizeInfo.annoInfo.alwaysRefresh) {
            return "{\n" +
                    "        return com.trend.lazyinject.lib.LazyInject.getComponent(${componentType}.class);\n" +
                    "    }"
        } else {
            return "if (${fieldName} != null) {\n" +
                    "            return ${fieldName};\n" +
                    "        } else {\n" +
                    "            ${fieldName} = com.trend.lazyinject.lib.LazyInject.getComponent(${componentType}.class);\n" +
                    "            return ${fieldName};\n" +
                    "        }"
        }
    }

    public static String getMethodName(CtField field) {
        return "lazyInject_autoGen_Get_${field.getName()}"
    }

    public static boolean isLzOptimizedMethod(CtMethod ctMethod) {
        return ctMethod.getName().startsWith("lazyInject_autoGen_Get_")
    }

    public static boolean isLzOptimizedMethod(String ctMethod) {
        if (ctMethod == null)
            return false
        return ctMethod.startsWith("lazyInject_autoGen_Get_")
    }

    class InjectOptimizeInfo {

        InjectAnnoInfo annoInfo
        CtMethod providerMethod

        InjectOptimizeInfo(InjectAnnoInfo annoInfo, CtMethod providerMethod) {
            this.annoInfo = annoInfo
            this.providerMethod = providerMethod
        }
    }

    class InjectComponentOptimizeInfo {

        InjectComponentAnnoInfo annoInfo

        InjectComponentOptimizeInfo(InjectComponentAnnoInfo annoInfo) {
            this.annoInfo = annoInfo
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

    InjectComponentOptimizeInfo getInjectComponentOptimizeInfo(CtField field) {
        InjectComponentAnnoInfo annoInfo = AnnotationParser.getInjectComponentInfo(field, classPool)
        if (annoInfo == null)
            return null
        if (annoInfo.nullProtect)
            return null
        if (annoInfo.value != "null")
            return null
        return new InjectComponentOptimizeInfo(annoInfo)
    }

    interface MakeMethodCallback {
        CtMethod makeMethod()
    }

}
