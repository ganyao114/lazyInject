package com.trend.lazyinject.aopweave.weave

import com.trend.lazyinject.aopweave.annotation.AnnotationParser
import javassist.Modifier
import javassist.expr.FieldAccess

public class InjectWeave {

    public static void inject(FieldAccess f) {
        String fieldName = f.field.name
        String fieldType = f.field.getType().name
        String fieldDeclareClass = f.field.declaringClass.name
        String isStatic = Modifier.isStatic(f.field.getModifiers()) ? "true" : "false"
        String injectInfo = AnnotationParser.getInjectInfoStr(f.field.getFieldInfo())
        String fieldGet = fieldDeclareClass + ".class.getDeclaredField(\"${fieldName}\")"
        f.replace("\$_ = (${fieldType})com.trend.lazyinject.lib.weave.FieldGetHook.hookInject(${isStatic}, \$0, ${fieldDeclareClass}.class,${fieldGet}, ${fieldType}.class, ${injectInfo});")
    }

}
