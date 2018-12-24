package com.trend.lazyinject.aopweave

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.WeaveClassPool
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.api.logging.Logger

public class WeavePluginEntry extends Transform implements Plugin<Project> {

    Project project
    static Logger logger

    @Override
    void apply(Project project) {
        this.project = project
        project.android.registerTransform(this)
        logger = project.logger
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

        

    }
}
