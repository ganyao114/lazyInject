package javassist;

public class WeaveClassPool extends ClassPool {

    @Override
    protected CtClass createCtClass(String classname, boolean useCache) {
        CtClass ctClass = super.createCtClass(classname, useCache);
        if (ctClass instanceof CtClassType) {
            return new WeaveClass(classname, this);
        } else {
            return ctClass;
        }
    }

}
