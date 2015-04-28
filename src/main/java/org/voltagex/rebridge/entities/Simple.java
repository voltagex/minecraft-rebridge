package org.voltagex.rebridge.entities;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.util.Objects;

public class Simple extends ServiceResponse
{

    private String key;
    private String value;

    /**
     * Represents a single key => value
     */
    public Simple()
    {
    }

    /**
     * Represents a single key => value
     * @param Key
     * @param Value
     */
    public Simple(String Key, String Value)
    {
        key = Key;
        value = Value;
    }

    public String getKey() { return key; }
    public void setKey(String value) { this.key = value; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public void setKeyValue(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

}
