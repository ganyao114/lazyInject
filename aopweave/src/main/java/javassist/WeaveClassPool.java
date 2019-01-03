package javassist;

public class WeaveClassPool extends ClassPool {

    public WeaveClassPool() {
    }

    public WeaveClassPool(boolean useDefaultPath) {
        super(useDefaultPath);
    }

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
