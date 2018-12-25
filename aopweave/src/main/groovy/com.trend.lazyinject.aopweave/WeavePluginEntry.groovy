package com.trend.lazyinject.aopweave

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import com.trend.lazyinject.annotation.Inject
import com.trend.lazyinject.annotation.InjectComponent
import com.trend.lazyinject.aopweave.annotation.AnnotationParser
import com.trend.lazyinject.aopweave.classes.ClassContainer
import com.trend.lazyinject.aopweave.classes.ClassGetter
import com.trend.lazyinject.aopweave.config.WeaveConfig
import javassist.CannotCompileException
import javassist.CtClass
import javassist.Modifier
import javassist.NotFoundException
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

        project.extensions.create("lazyinject",WeaveConfig)
        logger = project.logger
        config = project.lazyinject

        project.android.registerTransform(this)
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
        def name = QualifiedContent.Scope.PROJECT_LOCAL_DEPS.name()
        def deprecated = QualifiedContent.Scope.PROJECT_LOCAL_DEPS.getClass()
                .getField(name).getAnnotation(Deprecated.class)

        if (deprecated == null) {
            println "cannot find QualifiedContent.Scope.PROJECT_LOCAL_DEPS Deprecated.class "
            return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.Scope.PROJECT
                    , QualifiedContent.Scope.PROJECT_LOCAL_DEPS
                    , QualifiedContent.Scope.EXTERNAL_LIBRARIES
                    , QualifiedContent.Scope.SUB_PROJECTS
                    , QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS)
        } else {
            println "find QualifiedContent.Scope.PROJECT_LOCAL_DEPS"
            return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.Scope.PROJECT
                    , QualifiedContent.Scope.EXTERNAL_LIBRARIES
                    , QualifiedContent.Scope.SUB_PROJECTS)
        }
    }

    @Override
    boolean isIncremental() {
        return true
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

        if (config.enable) {

            new ForkJoinPool().submit {
                classContainer.classes.parallelStream().findAll { ctClass ->
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
                                        if (f.reader) {
                                            String fieldName = f.field.name
                                            String fieldType = f.field.getType().name
                                            String fieldDeclareClass = f.field.declaringClass.name
                                            String isStatic = Modifier.isStatic(f.field.getModifiers()) ? "true" : "false"
                                            String injectInfo = AnnotationParser.getInjectInfo(f.field.getFieldInfo())
                                            String fieldGet = fieldDeclareClass + ".class.getDeclaredField(\"${fieldName}\")"
                                            f.replace("\$_ = (${fieldType})com.trend.lazyinject.annotation.FieldGetHook.hookInject(${isStatic}, \$0, ${fieldDeclareClass}.class,${fieldGet}, ${fieldType}.class, ${injectInfo});")
                                        }
                                    } else if (injectComponent != null) {
                                        if (f.reader) {
                                            String fieldName = f.field.name
                                            String fieldType = f.field.getType().name
                                            String fieldDeclareClass = f.field.declaringClass.name
                                            String isStatic = Modifier.isStatic(f.field.getModifiers()) ? "true" : "false"
                                            String injectInfo = AnnotationParser.getInjectComponentInfo(f.field.getFieldInfo())
                                            String fieldGet = fieldDeclareClass + ".class.getDeclaredField(\"${fieldName}\")"
                                            f.replace("\$_ = (${fieldType})com.trend.lazyinject.annotation.FieldGetHook.hookInjectComponent(${isStatic}, \$0, ${fieldDeclareClass}.class,${fieldGet}, ${fieldType}.class, ${injectInfo});")
                                        }
                                    }
                                }
                            })
                        }
                    }
                })
            }.get()

        }

        new ForkJoinPool().submit {
            classContainer.classes.parallelStream().findAll { ctClass ->
                ctClass.writeFile(outputDir.absolutePath)
            }
        }.get()

        new ForkJoinPool().submit {
            classContainer.classesNotScan.parallelStream().findAll { ctClass ->
                ctClass.writeFile(outputDir.absolutePath)
            }
        }.get()

    }
}
