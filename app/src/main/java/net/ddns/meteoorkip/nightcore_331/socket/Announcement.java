package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Announcement {

    private final JSONObject values;

    public Announcement(JSONObject values) {
        this.values = values;
    }

    public Boolean getEnabled() throws JSONException {
        return values.getBoolean(Variable.enabled.name());
    }

    public Boolean getStatic() throws JSONException {
        return values.getBoolean("static");
    }

    public String getText() throws JSONException {
        return values.getString(Variable.text.name());
    }

    public enum Variable {
        enabled, text
    }
}
