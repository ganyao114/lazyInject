package com.trend.lazyinject.aopweave.annotation

import com.trend.lazyinject.annotation.Inject
import com.trend.lazyinject.annotation.InjectComponent
import com.trend.lazyinject.aopweave.infos.InjectAnnoInfo
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.FieldInfo
import javassist.bytecode.annotation.*

public class AnnotationParser {
    public static String getInjectInfoStr(FieldInfo fieldInfo) {

        AnnotationsAttribute attribute = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag)
        Annotation annotation = attribute.getAnnotation(Inject.class.getName())
        ClassMemberValue componentValue = annotation.getMemberValue("component")
        BooleanMemberValue refreshValue = annotation.getMemberValue("alwaysRefresh")
        BooleanMemberValue nullProtectValue = annotation.getMemberValue("nullProtect")
        ArrayMemberValue argsValue = annotation.getMemberValue("args")

        String component = componentValue == null ? Inject.None.getCanonicalName() + ".class" : componentValue.toString()
        String refreshed = refreshValue == null ? "false" : refreshValue.toString()
        String nullProtect = nullProtectValue == null ? "false" : nullProtectValue.toString()
        String args = argsValue == null ? "null" : "new String[]" + argsValue.toString()

        return "new com.trend.lazyinject.annotation.InjectInfo(${component}, ${refreshed}, ${nullProtect}, ${args})"

    }

    public static String getInjectComponentInfoStr(FieldInfo fieldInfo) {

        AnnotationsAttribute attribute = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag)
        Annotation annotation = attribute.getAnnotation(InjectComponent.class.getName())
        StringMemberValue vValue = annotation.getMemberValue("value")
        BooleanMemberValue refreshValue = annotation.getMemberValue("alwaysRefresh")
        BooleanMemberValue nullProtectValue = annotation.getMemberValue("nullProtect")

        String value = vValue == null ? "\"\"" : vValue.toString()
        String refreshed = refreshValue == null ? "false" : refreshValue.toString()
        String nullProtect = nullProtectValue == null ? "false" : nullProtectValue.toString()

        return "new com.trend.lazyinject.annotation.InjectComponentInfo(${value}, ${refreshed}, ${nullProtect})"

    }

    public static InjectAnnoInfo getInjectInfo(CtField field, ClassPool classPool) {

        AnnotationsAttribute attribute = (AnnotationsAttribute) field.fieldInfo.getAttribute(AnnotationsAttribute.visibleTag)
        Annotation annotation = attribute.getAnnotation(Inject.class.getName())
        ClassMemberValue componentValue = annotation.getMemberValue("component")
        BooleanMemberValue refreshValue = annotation.getMemberValue("alwaysRefresh")
        BooleanMemberValue nullProtectValue = annotation.getMemberValue("nullProtect")
        ArrayMemberValue argsValue = annotation.getMemberValue("args")

        String component = componentValue == null ? Inject.None.name : componentValue.getValue()

        InjectAnnoInfo info = new InjectAnnoInfo()

        if (Inject.None.name.equals(component)) {
            info.component = field.type.declaringClass
            if (info.component == null) {
                info.component = getEnclosingClass(field.type, classPool)
            }
            if (info.component == null)
                return null
        } else {
            info.component = classPool.get(component)
        }

        info.alwaysRefresh = refreshValue == null ? false : refreshValue.value
        info.nullProtect = nullProtectValue == null ? false : nullProtectValue.value
        info.args = argsValue == null ? "null" : "new String[]" + argsValue.toString()

        return info
    }

    public static CtClass getEnclosingClass(CtClass ctClass, ClassPool classPool) {
        String className = ctClass.name
        int index = className.lastIndexOf('$')
        if (index < 0)
            return null
        className = className.substring(0, index - 1)
        return classPool.get(className)
    }

}
