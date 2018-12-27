package com.trend.lazyinject.aopweave.weave

import com.trend.lazyinject.aopweave.annotation.AnnotationParser
import javassist.Modifier
import javassist.expr.FieldAccess

public class InjectComponentWeave {

    public static void inject(FieldAccess f) {
        String fieldName = f.field.name
        String fieldType = f.field.getType().name
        String fieldDeclareClass = f.field.declaringClass.name
        String isStatic = Modifier.isStatic(f.field.getModifiers()) ? "true" : "false"
        String injectInfo = AnnotationParser.getInjectComponentInfoStr(f.field.getFieldInfo())
        String fieldGet = fieldDeclareClass + ".class.getDeclaredField(\"${fieldName}\")"
        f.replace("\$_ = (${fieldType})com.trend.lazyinject.lib.weave.FieldGetHook.hookInjectComponent(${isStatic}, \$0, ${fieldDeclareClass}.class,${fieldGet}, ${fieldType}.class, ${injectInfo});")
    }

}
