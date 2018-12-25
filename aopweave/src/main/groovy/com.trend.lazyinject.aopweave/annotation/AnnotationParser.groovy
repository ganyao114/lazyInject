package com.trend.lazyinject.aopweave.annotation

import com.trend.lazyinject.annotation.Inject
import com.trend.lazyinject.annotation.InjectComponent
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.FieldInfo
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.ArrayMemberValue
import javassist.bytecode.annotation.BooleanMemberValue
import javassist.bytecode.annotation.ClassMemberValue
import javassist.bytecode.annotation.StringMemberValue

public class AnnotationParser {
    public static String getInjectInfo(FieldInfo fieldInfo) {

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

    public static String getInjectComponentInfo(FieldInfo fieldInfo) {

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
}
