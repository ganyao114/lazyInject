package com.trend.lazyinject.aopweave.annotation

import com.trend.lazyinject.annotation.InjectTest
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.FieldInfo
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.ArrayMemberValue
import javassist.bytecode.annotation.BooleanMemberValue
import javassist.bytecode.annotation.ClassMemberValue

public class AnnotationParser {
    public static String getInjectInfo(FieldInfo fieldInfo) {

        AnnotationsAttribute attribute = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag)
        Annotation annotation = attribute.getAnnotation(InjectTest.class.getName())
        ClassMemberValue componentValue = annotation.getMemberValue("component")
        BooleanMemberValue refreshValue = annotation.getMemberValue("alwaysRefresh")
        BooleanMemberValue nullProtect = annotation.getMemberValue("nullProtect")
        ArrayMemberValue argsValue = annotation.getMemberValue("args")

        return "new com.trend.lazyinject.annotation.InjectInfo(${componentValue.toString()}, ${refreshValue.toString()}, ${nullProtect.toString()}, new String[]${argsValue.toString()})"

    }
}
