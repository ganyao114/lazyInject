package com.trend.lazyinject.aopweave

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.trend.lazyinject.annotation.InjectTest
import com.trend.lazyinject.aopweave.classes.ClassContainer
import com.trend.lazyinject.aopweave.classes.ClassGetter
import com.trend.lazyinject.aopweave.config.WeaveConfig
import javassist.CannotCompileException
import javassist.CtClass
import javassist.WeaveClassPool
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer

public class WeavePluginEntry extends Transform implements Plugin<Project> {

    Project project
    static Logger logger
    WeaveConfig config

    @Override
    void apply(Project project) {
        this.project = project
        project.android.registerTransform(this)
        project.extensions.create("lazyinject",WeaveConfig)
        logger = project.logger
        config = project.lazyinject
    }

    @Override
    String getName() {
        return "lazyinject-weave"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        Context context = transformInvocation.context
        Collection<TransformInput> inputs = transformInvocation.inputs
        Collection<TransformInput> referencedInputs = transformInvocation.referencedInputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        boolean isIncremental = transformInvocation.incremental

        WeaveClassPool classPool = new WeaveClassPool()

        project.android.bootClasspath.each {
            classPool.appendClassPath((String)it.absolutePath)
        }

        ClassContainer classContainer = ClassGetter.toCtClasses(inputs, classPool, config)

        doWeave(classContainer, outputProvider.getContentLocation("main", outputTypes, scopes, Format.DIRECTORY))



    }

    void doWeave(ClassContainer classContainer, File outputDir) {

        new ForkJoinPool().submit {
            classContainer.classes.parallelStream().findAll { ctClass ->
                if (ctClass.isInterface())
                    return false
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
                                super.edit(f)
                                if (f.field == null)
                                    return
                                InjectTest injectTest = f.field.getAnnotation(InjectTest.class)
                                String name = f.field.name
                                if (injectTest == null)
                                    return
                                f.replace("\$_ = com.trend.lazyinject.annotation.FieldGetHook.hookInject(\$0, \$class, null, ${name}, \$type);")
                            }
                        })
                    }
                    ctClass.writeFile(outputDir.absolutePath)
                }
            })
        }.get()
    }
}
