package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Log {

    private final JSONObject values;

    public Log(JSONObject values) {
        this.values = values;
    }

    public String getUserName() throws JSONException {
        return values.getString(Variable.username.name());
    }

    public String getText() throws JSONException {
        return values.getString(Variable.text.name());
    }

    public int getRank() throws JSONException {
        return values.getInt(Variable.rank.name());
    }

    public enum Variable {
        username, text, rank
    }
}
