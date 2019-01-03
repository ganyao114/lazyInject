package com.trend.lazyinject.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.ComponentImpl;
import com.trend.lazyinject.annotation.NoCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ComponentProcessor extends AbstractProcessor {

    private final static String DEFAULT_PKG = "com.trend.lazyinject.buildmap";
    private final static String DEFAULT_CLASS_NAME = "Auto_ComponentBuildMap";

    private static final boolean DEBUG = false;
    private Messager messager;
    private Filer filer;
    private Types types;
    private Map<String, String> options;
    private String targetPkg;
    private String targetClassName;
    private String defaultComponent;

    private Map<String,Map<String,ComponentEntity>> componentMap = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> ret = new HashSet<>();
        ret.add(ComponentImpl.class.getCanonicalName());
        ret.add(Component.class.getCanonicalName());
        return ret;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        types = processingEnvironment.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        options = processingEnvironment.getOptions();
        targetPkg = options.getOrDefault("targetPackage", DEFAULT_PKG);
        defaultComponent = options.getOrDefault("defaultComponent", "default");
        targetClassName = options.getOrDefault("targetClassName", DEFAULT_CLASS_NAME);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            return false;
        }

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ComponentImpl.class);

        if (elements != null && elements.size() > 0) {
            TypeSpec.Builder buildMapSpec = TypeSpec.classBuilder(targetClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ClassName.get("android.support.annotation", "Keep"));
            for (Element element:elements) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement typeElement = (TypeElement) element;
                    addBuildEntity(typeElement);
                }
            }
            addBuildMethod(buildMapSpec);
            try {
                JavaFile.builder(targetPkg, buildMapSpec.build())
                        .addStaticImport(ClassName.get("com.trend.lazyinject.lib.component", "ComponentBuilder"), "doBuild")
                        .build()
                        .writeTo(filer);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void addBuildEntity(TypeElement typeElement) {
        String componentName = null;
        String componentSimpleName = null;
        String pkgName = null;
        ComponentImpl componentImpl = typeElement.getAnnotation(ComponentImpl.class);
        String name = componentImpl.name();
        String componentClazzName = componentImpl.component();
        boolean cache = componentImpl.cache();
        if (componentClazzName != null && componentClazzName.length() > 0) {
            componentName = componentClazzName;
            componentSimpleName = componentName.substring(componentName.lastIndexOf('.') + 1, componentName.length());
        } else {
            TypeElement componentType = getComponentType(typeElement);
            if (componentType != null) {
                componentName = componentType.getQualifiedName().toString();
                componentSimpleName = componentType.getSimpleName().toString();
            }
        }

        if (componentName != null) {
            ComponentEntity entity = new ComponentEntity();
            entity.simpleName = componentSimpleName;
            entity.component = componentName;
            entity.componentImpl = typeElement.getQualifiedName().toString();
            entity.cache = cache;
            entity.name = name;
            Map<String,ComponentEntity> entityMap = componentMap.get(componentName);
            if (entityMap == null) {
                entityMap = new HashMap<>();
                componentMap.put(componentName, entityMap);
            }
            entityMap.put(name, entity);
        }
    }

    private void addBuildMethod(TypeSpec.Builder buildMapSpec) {

        for (Map<String,ComponentEntity> entityMap:componentMap.values()) {

            ComponentEntity entity = entityMap.get(defaultComponent);

            if (entity == null) {
                entity = entityMap.get("default");
            }

            if (entity == null) {
                entity = entityMap.values().iterator().next();
            }

            if (entity == null)
                continue;

            String pkgName = entity.component.substring(0, entity.component.lastIndexOf('.'));
            if (pkgName != null) {
                String implSimpleName = entity.componentImpl.substring(entity.componentImpl.lastIndexOf('.') + 1, entity.componentImpl.length());
                ClassName className = ClassName.get(pkgName, entity.simpleName);
                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("build" + implSimpleName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(className)
                        .addStatement("return doBuild(" + className + ".class, " + entity.componentImpl + ".class)");
                if (!entity.cache) {
                    methodSpec.addAnnotation(NoCache.class);
                }
                buildMapSpec.addMethod(methodSpec.build());
            }
        }
    }

    private TypeElement getComponentType(TypeElement implType) {
        try {
            return doGetComponentType(implType);
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "ComponentProcessor can not find component of " + implType.getQualifiedName().toString() + "\n -----error msg-----\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public TypeElement doGetComponentType(TypeElement implType) {
        if (implType.getAnnotation(Component.class) != null)
            return implType;
        TypeMirror superclassMirror = implType.getSuperclass();
        if (superclassMirror != null) {
            TypeElement superType = (TypeElement) types.asElement(superclassMirror);
            if (superType != null && !"java.lang.Object".equals(superType.getQualifiedName().toString())) {
                TypeElement componentType = doGetComponentType(superType);
                if (componentType != null)
                    return componentType;
            }
        }
        List<? extends TypeMirror> interfacesMirrors = implType.getInterfaces();
        if (interfacesMirrors != null) {
            for (TypeMirror mirror : interfacesMirrors) {
                TypeElement typeElement = (TypeElement) types.asElement(mirror);
                if (typeElement == null || "java.lang.Object".equals(typeElement.getQualifiedName().toString()))
                    continue;
                TypeElement componentType = getComponentTypeByInterface(typeElement);
                if (componentType != null)
                    return componentType;
            }
        }
        return null;
    }

    private TypeElement getComponentTypeByInterface(TypeElement implType) {
        if (implType.getAnnotation(Component.class) != null) {
            return implType;
        } else {
            List<? extends TypeMirror> interfacesMirrors = implType.getInterfaces();
            if (interfacesMirrors != null) {
                for (TypeMirror mirror:interfacesMirrors) {
                    TypeElement typeElement = (TypeElement) types.asElement(mirror);
                    if (typeElement == null)
                        continue;
                    return getComponentTypeByInterface(typeElement);
                }
            }
            return null;
        }
    }

}
