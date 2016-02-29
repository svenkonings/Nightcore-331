package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRemove {

    private final JSONObject values;

    public UserRemove(JSONObject values) {
        this.values = values;
    }

    public int getUserId() throws JSONException {
        return values.getInt(Variable.userid.name());
    }

    public String getUserName() throws JSONException {
        return values.getString(Variable.username.name());
    }

    public enum Variable {
        userid, username
    }
}
