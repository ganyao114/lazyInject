package com.trend.lazyinject.aopweave.transforms

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import com.trend.lazyinject.annotation.Inject
import com.trend.lazyinject.annotation.InjectComponent
import com.trend.lazyinject.aopweave.classes.JavassistFile
import com.trend.lazyinject.aopweave.config.WeaveConfig
import com.trend.lazyinject.aopweave.jar.ClassFilter
import com.trend.lazyinject.aopweave.optimize.InliningOptimize
import com.trend.lazyinject.aopweave.weave.InjectComponentWeave
import com.trend.lazyinject.aopweave.weave.InjectWeave
import javassist.CannotCompileException
import javassist.CtBehavior
import javassist.CtClass
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.gradle.api.Project

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer

public class LazyInjectTransform extends JavassistTransform {

    static String NAME = "lazyInject"

    WeaveConfig config
    InliningOptimize inliningOptimize

    LazyInjectTransform(Project project) {
        super(project)
        project.extensions.create(NAME,WeaveConfig)
        config = project.lazyinject
        inliningOptimize = new InliningOptimize(container.classPool)
    }

    @Override
    void doInject(TransformInvocation invocation) {

        if (container.fileMap.isEmpty())
            return

        container.fileMap.entrySet().parallelStream().forEach {
            String filePath = it.key
            JavassistFile javassistFile = it.value
            if (javassistFile.ctClassMap != null) {
                //.jar

            } else if (javassistFile.ctClass != null) {
                //.class
                javassistFile.ctClass.declaredBehaviors.each {
                    it.instrument(new ExprEditor() {
                        @Override
                        void edit(FieldAccess f) throws CannotCompileException {
                            doInjectField(filePath, javassistFile.ctClass, f)
                        }
                    })
                }
            }
        }

        new ForkJoinPool().submit {
            container.fileMap.entrySet().parallelStream().findAll { ctClass ->
                if (ctClass.isInterface()) {
                    return false
                }
                try {
                    ctClass.getSuperclass()
                    return true
                } catch (Exception e) {
                    return false
                }
            }.forEach(new Consumer<CtClass>() {
                @Override
                void accept(CtClass ctClass) {
                    ctClass.declaredBehaviors.each {
                        it.instrument(new ExprEditor() {
                            @Override
                            void edit(FieldAccess f) throws CannotCompileException {

                            }
                        })
                    }
                }
            })
        }.get()
    }

    void doInjectField(String filePath, CtClass ctClass, FieldAccess f) {
        try {
            if (f.field == null)
                return
        } catch (NotFoundException e) {
            return
        }
        if (f.field == null)
            return
        Inject inject = f.field.getAnnotation(Inject.class)
        InjectComponent injectComponent = f.field.getAnnotation(InjectComponent.class)
        if (inject != null) {
            CtBehavior where = f.where()
            if (where != null) {
                if (InliningOptimize.isLzOptimizedMethod(where.name)) {
                    return
                }
            }
            if (f.reader) {
                if (config.optimize) {
                    if (!inliningOptimize.optimize(f, false)) {
                        InjectWeave.inject(f)
                    }
                } else {
                    InjectWeave.inject(f)
                }
            }
        } else if (injectComponent != null) {
            CtBehavior where = f.where()
            if (where != null) {
                if (InliningOptimize.isLzOptimizedMethod(where.name)) {
                    return
                }
            }
            if (f.reader) {
                if (config.optimize) {
                    if (!inliningOptimize.optimize(f, true)) {
                        InjectComponentWeave.inject(f)
                    }
                } else {
                    InjectComponentWeave.inject(f)
                }
            }
        }
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean filter(String classPath) {
        return ClassFilter.filterClass(classPath, config.includes)
    }

    @Override
    boolean filter(JarInput jarInput) {
        return ClassFilter.filterJar(jarInput, config.includes)
    }
}
