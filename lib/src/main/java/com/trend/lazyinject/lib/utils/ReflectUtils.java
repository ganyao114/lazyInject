package com.trend.lazyinject.lib.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * Created by ganyao on 2017/12/4.
 */

public class ReflectUtils {

    public final static Class<?> getRawType(Type type) {
        checkNotNull(type, "type == null");
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    public final static boolean equals(Type a, Type b) {
        if (a == b) {
            return true; // Also handles (a == null && b == null).
        } else if (a instanceof Class) {
            if (b instanceof ParameterizedType) {
                if (equal(getRawType(a), getRawType(b))) {
                    ParameterizedType pb = (ParameterizedType) b;
                    Type[] types = pb.getActualTypeArguments();
                    if (types == null || types.length == 0)
                        return true;
                    for (Type type : types) {
                        if (!type.equals(Object.class)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return a.equals(b); // Class already specifies equals().
            }
        } else if (a instanceof ParameterizedType) {
            if (b instanceof ParameterizedType) {
                ParameterizedType pa = (ParameterizedType) a;
                ParameterizedType pb = (ParameterizedType) b;
                return equal(pa.getOwnerType(), pb.getOwnerType())
                        && pa.getRawType().equals(pb.getRawType())
                        && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
            } else {
                if (b instanceof Class) {
                    if (equal(getRawType(a), getRawType(b))) {
                        ParameterizedType pa = (ParameterizedType) a;
                        Type[] types = pa.getActualTypeArguments();
                        if (types == null || types.length == 0)
                            return true;
                        for (Type type : types) {
                            if (!type.equals(Object.class)) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) return false;
            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) return false;
            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) return false;
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());
        } else {
            return false; // This isn't a type we support!
        }
    }

    /**
     * Returns true if two possibly-null objects are equal.
     */
    public final static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    private final static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public final static Field getField(Class outter, String name, Class<? extends Annotation> annoType) {
        Class clazz = outter;
        while (clazz != null && !clazz.equals(Object.class)) {
            Field field = null;
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
            clazz = clazz.getSuperclass();
            if (field == null)
                continue;
            if (field.isAnnotationPresent(annoType))
                return field;
        }
        return null;
    }

    /**
     * for DI
     * test impl can be cast to inter
     *
     * @param inter
     * @param impl
     * @return
     * @author swift_gan
     */
    public final static boolean canCast(Type inter, Type impl) {

        if (inter.equals(impl))
            return true;

        if (inter instanceof Class && impl instanceof Class) {
            return ((Class) inter).isAssignableFrom((Class<?>) impl);
        }

        if (impl instanceof WildcardType) {
            Type[] type = ((WildcardType) impl).getUpperBounds();
            if (ValidateUtil.isEmpty(type))
                return canCast(inter, Object.class);
            for (Type t : type) {
                if (canCast(inter, t)) {
                    return true;
                }
            }
            return false;
        }

        if (inter instanceof WildcardType) {
            Type[] type = ((WildcardType) impl).getLowerBounds();
            if (ValidateUtil.isEmpty(type))
                return canCast(Object.class, impl);
            for (Type t : type) {
                if (canCast(t, impl)) {
                    return true;
                }
            }
            return false;
        }

        if (impl instanceof TypeVariable) {
            Type[] type = ((TypeVariable) impl).getBounds();
            if (ValidateUtil.isEmpty(type))
                return canCast(inter, Object.class);
            for (Type t : type) {
                if (canCast(inter, t)) {
                    return true;
                }
            }
            return false;
        }

        if (inter instanceof TypeVariable) {
            Type[] type = ((TypeVariable) inter).getBounds();
            if (ValidateUtil.isEmpty(type))
                return canCast(Object.class, impl);
            for (Type t : type) {
                if (canCast(t, impl)) {
                    return true;
                }
            }
            return false;
        }

        if (inter instanceof ParameterizedType) {
            Type[] interTypes = ((ParameterizedType) inter).getActualTypeArguments();
            if (impl instanceof Class) {
                if (canCast(getRawType(inter), impl)) {
                    return isObjectType(interTypes);
                } else {
                    return false;
                }
            } else if (impl instanceof ParameterizedType) {
                if (canCast(getRawType(inter), getRawType(impl))) {
                    Type[] implTypes = ((ParameterizedType) impl).getActualTypeArguments();
                    if (implTypes == null || interTypes == null || interTypes.length != implTypes.length)
                        return false;
                    for (int i = 0; i < interTypes.length; i++) {
                        if (!canCast(interTypes[i], implTypes[i])) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (inter instanceof Class) {
            if (impl instanceof ParameterizedType) {
                if (canCast(inter, getRawType(impl))) {
                    Type[] implTypes = ((ParameterizedType) impl).getActualTypeArguments();
                    return isObjectType(implTypes);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (inter instanceof GenericArrayType) {
            if (impl instanceof GenericArrayType) {
                return canCast(((GenericArrayType) inter).getGenericComponentType(), ((GenericArrayType) impl).getGenericComponentType());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private final static boolean isObjectType(Type... types) {
        if (types == null)
            return true;
        for (Type type : types) {
            if (!type.equals(Object.class))
                return false;
        }
        return true;
    }

}
