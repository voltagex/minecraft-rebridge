package org.voltagex.rebridge.entities;

import fi.iki.elonen.NanoHTTPD;

import java.util.ArrayList;
import java.util.List;

//todo: this sucks.
public abstract class ListResponse<T> extends ServiceResponse
{
    abstract ArrayList<T> getList();
}
