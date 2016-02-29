package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class DeleteChat {

    private final JSONObject values;

    public DeleteChat(JSONObject values) {
        this.values = values;
    }

    public int getCid() throws JSONException {
        return values.getInt(Variable.cid.name());
    }

    public enum Variable {
        cid
    }
}
