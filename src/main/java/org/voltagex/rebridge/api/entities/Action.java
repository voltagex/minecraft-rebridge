package org.voltagex.rebridge.api.entities;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

//todo: rename this
public class Action
{
    //todo: Replace with Class<T>
    public Action(Constructor<?> controller, Method method, List<String> parameters)
    {
        this.controller = controller;
        this.method = method;
        this.parameters = parameters;
    }

    public Constructor<?> getController()
    {
        return controller;
    }

    public void setController(Constructor<?> controller)
    {
        this.controller = controller;
    }

    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<String> parameters)
    {
        this.parameters = parameters;
    }

    private Constructor<?> controller;
    private Method method;
    private List<String> parameters;


}
