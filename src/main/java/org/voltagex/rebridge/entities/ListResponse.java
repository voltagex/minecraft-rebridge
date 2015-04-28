package org.voltagex.rebridge.entities;

import java.util.List;

//todo: this sucks.
public class ListResponse<T> extends ServiceResponse
{
    protected List<T> containedList;

    public ListResponse(List<T> list)
    {
        containedList = list;
    }

    public List<T> getList()
    {
        return containedList;
    }
}
