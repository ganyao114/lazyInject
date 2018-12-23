package javassist;

import java.io.IOException;
import java.io.InputStream;

import javassist.bytecode.ClassFile;

public class WeaveClass extends CtClassType {

    private CtMember.Cache members;


    WeaveClass(String name, ClassPool cp) {
        super(name, cp);
    }

    WeaveClass(InputStream ins, ClassPool cp) throws IOException {
        super(ins, cp);
    }

    WeaveClass(ClassFile cf, ClassPool cp) {
        super(cf, cp);
    }

    @Override
    protected synchronized CtMember.Cache getMembers() {
        if (members == null)
            members = super.getMembers();
        return members;
    }

    @Override
    void compress() {
//        super.compress();
        // not do it
    }

    @Override
    public Object getAnnotation(Class clz) throws ClassNotFoundException {
        try {
            return super.getAnnotation(clz);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    public CtBehavior[] getDeclaredBehaviors() {
        try {
            return super.getDeclaredBehaviors();
        } catch (Exception e) {
            // ignore
        }
        return new CtBehavior[0];
    }

}
