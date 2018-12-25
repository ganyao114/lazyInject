package com.trend.lazyinject.aopweave.classes

import com.android.SdkConstants
import com.android.build.api.transform.TransformInput
import com.trend.lazyinject.aopweave.config.WeaveConfig
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils

import java.util.jar.JarFile

class ClassGetter {

    static ClassContainer toCtClasses(Collection<TransformInput> inputs, ClassPool classPool, WeaveConfig config) {
        HashSet<String> classNames = new HashSet<>()
        def startTime = System.currentTimeMillis()
        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                classPool.insertClassPath(it.file.absolutePath)
                FileUtils.listFiles(it.file, null, true).each {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className = it.absolutePath.substring(dirPath.length() + 1, it.absolutePath.length() - SdkConstants.DOT_CLASS.length())
                                .replaceAll('/', '.').replaceAll('\\\\','.');
                        classNames.add(className)
                    }
                }
            }

            it.jarInputs.stream().each {
                def jarFile = new JarFile(it.file)
                classPool.insertClassPath(it.file.absolutePath)
                jarFile.stream().filter {
                    it.name.endsWith(SdkConstants.DOT_CLASS)
                }.each {
                    def className = it.name.substring(0, it.name.length() - SdkConstants.DOT_CLASS.length()).replaceAll('/', '.')
                    classNames.add(className)
                }
            }
        }
        def cost = (System.currentTimeMillis() -startTime) / 1000
        println "read all class file cost $cost second"
        ClassContainer classContainer = new ClassContainer()
        classNames.each {
            checkCtClass(classPool, classContainer, classPool.get(it), config)
        }

        return classContainer
    }


    static void checkCtClass(ClassPool classPool, ClassContainer container, CtClass ctClass, WeaveConfig config) {
        boolean needAdd = false
        if (config.includes == null || config.includes.length == 0) {
            needAdd = true
        } else {
            String className = ctClass.name
            for (String include:config.includes) {
                if (className.contains(include)) {
                    needAdd = true
                    break
                }
            }
        }
        if (needAdd) {
            container.classes.add(ctClass)
        } else {
            container.classesNotScan.add(ctClass)
        }
    }
}