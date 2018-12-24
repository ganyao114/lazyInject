package com.trend.lazyinject.aopweave.classes

import javassist.CtClass

public class ClassContainer {
    List<CtClass> classes = new ArrayList<>()
    List<CtClass> classesNotScan = new ArrayList<>();
}
