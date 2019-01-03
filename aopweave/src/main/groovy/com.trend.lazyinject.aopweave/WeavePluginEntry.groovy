package com.trend.lazyinject.aopweave

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import com.trend.lazyinject.annotation.Inject
import com.trend.lazyinject.annotation.InjectComponent
import com.trend.lazyinject.aopweave.classes.JavassistClassContainer
import com.trend.lazyinject.aopweave.classes.JavassistClassGetter
import com.trend.lazyinject.aopweave.config.WeaveConfig
import com.trend.lazyinject.aopweave.files.FileCopy
import com.trend.lazyinject.aopweave.optimize.InliningOptimize
import com.trend.lazyinject.aopweave.transforms.LazyInjectTransform
import com.trend.lazyinject.aopweave.weave.InjectComponentWeave
import com.trend.lazyinject.aopweave.weave.InjectWeave
import javassist.*
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
        return ImmutableSet.<QualifiedContent.Scope> of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
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

        File outputDirFile = outputProvider.getContentLocation("main", outputTypes, scopes, Format.DIRECTORY);

        if (config.enable) {

            long start = System.currentTimeMillis();

            logger.info("LazyInject - start weave")

            WeaveClassPool classPool = new WeaveClassPool()
            project.android.bootClasspath.each {
                classPool.appendClassPath((String) it.absolutePath)
            }
            JavassistClassContainer classContainer = JavassistClassGetter.toCtClasses(inputs, classPool, config)
            doWeave(classContainer, outputDirFile, classPool, isIncremental)

            logger.info("LazyInject - end weave - cost time:" + (System.currentTimeMillis() - start) + " ms")

        } else {
            if (isIncremental) {
                FileCopy.incrementalCopy(inputs, outputProvider)
            } else {
                FileCopy.fullCopy(inputs, outputProvider)
            }
        }

    }

    void doWeave(JavassistClassContainer classContainer, File outputDir, ClassPool classPool, boolean isIncremental) {

        if (config.enable) {

            InliningOptimize inliningOptimize = new InliningOptimize(classPool)

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
                                        CtBehavior where = f.where()
                                        if (where != null) {
                                            if (InliningOptimize.isLzOptimizedMethod(where.name)) {
                                                return
                                            }
                                        }
                                        if (f.reader) {
                                            if (config.optimize && !isIncremental) {
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
                                            if (config.optimize && !isIncremental) {
                                                if (!inliningOptimize.optimize(f, true)) {
                                                    InjectComponentWeave.inject(f)
                                                }
                                            } else {
                                                InjectComponentWeave.inject(f)
                                            }
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
