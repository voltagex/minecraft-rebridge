package org.voltagex.rebridge.api.entities;

@Deprecated
public class ObjectResponse extends ServiceResponse
{
    private Object returnedObject;
    public ObjectResponse(Object thing)
    {
        returnedObject = thing;
    }

    public Object getReturnedObject()
    {
        return returnedObject;
    }
}
