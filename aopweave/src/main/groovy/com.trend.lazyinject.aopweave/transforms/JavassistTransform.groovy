package com.trend.lazyinject.aopweave.transforms

import com.android.build.api.transform.TransformInvocation
import com.trend.lazyinject.aopweave.classes.JavassistClassContainer
import com.trend.lazyinject.aopweave.classes.JavassistClassGetter
import javassist.WeaveClassPool
import org.gradle.api.Project

import java.util.concurrent.ForkJoinPool

public abstract class JavassistTransform extends IncrementalTransform implements JavassistClassGetter.LoaderFilter {

    JavassistClassContainer container

    JavassistTransform(Project project) {
        super(project)
        container = new JavassistClassContainer(new WeaveClassPool(true))
    }

    @Override
    void loadClass(TransformInvocation invocation) {
        //setup classpath
        JavassistClassGetter.addClassPath(classPaths, container.classPool)
        //do load class
        new ForkJoinPool().submit {
            filesNeedInject.parallelStream().forEach {
                JavassistClassGetter.loadClass(it, container, this)
            }
        }.get()
    }


    @Override
    void diffClass(TransformInvocation invocation) {

    }
}
