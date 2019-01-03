package com.trend.lazyinject.aopweave.classes

import com.android.SdkConstants
import com.android.build.api.transform.TransformInput
import com.trend.lazyinject.aopweave.config.WeaveConfig
import javassist.ClassPool
import javassist.CtClass
import javassist.DirClassPath
import javassist.JarClassPath
import org.apache.commons.io.FileUtils

import java.util.jar.JarFile

class JavassistClassGetter {

    static JavassistClassContainer toCtClasses(Collection<TransformInput> inputs, ClassPool classPool, WeaveConfig config) {
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
        JavassistClassContainer classContainer = new JavassistClassContainer()
        classNames.each {
            checkCtClass(classPool, classContainer, classPool.get(it), config)
        }

        return classContainer
    }

    static loadClass(File file, JavassistClassContainer container, LoaderFilter filter) {
        ClassPool classPool = container.classPool
        if (file.isDirectory()) {
            FileUtils.listFiles(file, null, true).each {
                loadClass(it, classPool, container)
            }
        } else if (file.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
            if (filter.filter(file.absolutePath)) {
                CtClass ctClass = getCtClass(classPool, new FileInputStream(file))
                if (ctClass != null) {
                    container.addCtClass(file.absolutePath, null, ctClass)
                }
            }
        } else if (file.absolutePath.endsWith(SdkConstants.DOT_JAR)) {
            def jarFile = new JarFile(file)
            jarFile.stream().filter {
                it.name.endsWith(SdkConstants.DOT_CLASS)
            }.each {
                def className = it.name.substring(0, it.name.length() - SdkConstants.DOT_CLASS.length()).replaceAll('/', '.')
                if (filter.filter(className)) {
                    CtClass ctClass = classPool.get(className)
                    if (ctClass != null) {
                        container.addCtClass(file.absolutePath, it.name, ctClass)
                    }
                }
            }
        }
    }

    interface LoaderFilter {
        boolean filter(String classPath)
    }

    static CtClass getCtClass(ClassPool classPool, InputStream inputStream) {
        CtClass clazz
        try {
            clazz = classPool.makeClass(inputStream)
            return clazz
        } catch (RuntimeException e) {
            e.printStackTrace()
            //TODO 重复打开已冻结类，说明有重复类，跳过修改
            return clazz
        } finally {
            inputStream.close()
        }
    }


    static void checkCtClass(ClassPool classPool, JavassistClassContainer container, CtClass ctClass, WeaveConfig config) {
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
        ctClass.isModified()
        if (needAdd) {
            container.classes.add(ctClass)
        } else {
            container.classesNotScan.add(ctClass)
        }
    }

    static void addClassPath(List<File> files, ClassPool classPool) {
        //节省编译时的内存消耗
        ClassPool.doPruning = true
        for (File file : files) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    classPool.appendClassPath(new DirClassPath(file.absolutePath))
                } else {
                    classPool.appendClassPath(new JarClassPath(file.absolutePath))
                }
            }
        }
    }
}