package com.trend.lazyinject.aopweave.transforms

import com.android.build.api.transform.TransformInvocation
import com.trend.lazyinject.aopweave.classes.JavassistClassContainer
import com.trend.lazyinject.aopweave.classes.JavassistClassGetter
import com.trend.lazyinject.aopweave.classes.JavassistFile
import javassist.CtClass
import javassist.WeaveClassPool
import org.gradle.api.Project

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer

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
        if (container.fileMap.isEmpty())
            return
        container.fileMap.entrySet().parallelStream().forEach {
            String filePath = it.key
            JavassistFile javassistFile = it.value
            if (javassistFile.ctClassMap != null) {
                //.jar
                new ForkJoinPool().submit {
                    javassistFile.ctClassMap.entrySet().parallelStream().findAll { entry ->
                        CtClass ctClass = entry.value
                        if (ctClass.isInterface()) {
                            return false
                        }
                        try {
                            ctClass.getSuperclass()
                            return isClassDirty(ctClass.name)
                        } catch (Exception e) {
                            return false
                        }
                    }.forEach {
                        String jarEntryName = it.key
                        CtClass ctClass = it.value
                        byte[] classBytes = ctClass.toBytecode()
                        addDirtyClassBytes(filePath, jarEntryName, classBytes)
                    }
                }.get()
            } else if (javassistFile.ctClass != null) {
                //.class
                if (javassistFile.ctClass.isInterface()) {
                    return
                }
                try {
                    javassistFile.ctClass.getSuperclass()
                } catch (Exception e) {
                    return
                }
                if (isClassDirty(javassistFile.ctClass.name)) {
                    addDirtyClassBytes(filePath, null, javassistFile.ctClass.toBytecode())
                }
            }
        }
    }
}
