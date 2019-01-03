package com.trend.lazyinject.aopweave.classes

import javassist.ClassPool
import javassist.CtClass

import java.util.concurrent.ConcurrentHashMap

public class JavassistClassContainer {

    ClassPool classPool

    JavassistClassContainer() {
    }

    JavassistClassContainer(ClassPool classPool) {
        this.classPool = classPool
    }
    List<CtClass> classes = new ArrayList<>()
    List<CtClass> classesNotScan = new ArrayList<>()

    Map<String,JavassistFile> fileMap = new ConcurrentHashMap<>()
    List<File> classPath = new LinkedList<>()

    public void addCtClass(String filePath, String jarEntryName,CtClass ctClass) {
        JavassistFile javassistFile = fileMap.get(filePath)
        if (javassistFile == null) {
            synchronized (filePath.intern()) {
                javassistFile = fileMap.get(filePath)
                if (javassistFile == null) {
                    javassistFile = new JavassistFile()
                    if (jarEntryName != null && !jarEntryName.isEmpty()) {
                        javassistFile.ctClassMap = new ConcurrentHashMap<>()
                    }
                    fileMap.put(filePath, javassistFile)
                }
            }
        }
        if (javassistFile.ctClassMap != null) {
            javassistFile.ctClassMap.put(jarEntryName, ctClass)
        } else {
            javassistFile.ctClass = ctClass
        }
    }

    public void addClassPath(File path) {
        classPath << path
    }
}
