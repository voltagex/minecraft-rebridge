package org.voltagex.rebridge.predicates;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

/**
 * Created by Adam on 4/26/2015.
 */
public final class ReflectionFilters
{
    private ReflectionFilters()
    {

    }

    public static boolean nameNotContains(Method method, String nameContains)
    {
        return !(method.getName().toLowerCase().contains(nameContains.toLowerCase()));
    }

    public static boolean nameNotContains(Class aClass, String nameContains)
    {
        return !(aClass.getName().toLowerCase().contains(nameContains.toLowerCase()));
    }

    public static boolean nameContains(Method method, String nameContains)
    {
        return (method.getName().toLowerCase().contains(nameContains.toLowerCase()));
    }

    public static boolean nameContains(Class aClass, String nameContains)
    {
        return (aClass.getName().toLowerCase().contains(nameContains.toLowerCase()));
    }
}
