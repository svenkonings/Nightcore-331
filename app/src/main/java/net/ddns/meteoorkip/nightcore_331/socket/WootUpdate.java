package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class WootUpdate {

    private final JSONObject values;

    public WootUpdate(JSONObject values) {
        this.values = values;
    }

    public int getWoots() throws JSONException {
        return values.getInt(Variable.woots.name());
    }

    public int getMehs() throws JSONException {
        return values.getInt(Variable.mehs.name());
    }

    public int getGrabs() throws JSONException {
        return values.getInt(Variable.grabs.name());
    }

    public enum Variable {
        woots, mehs, grabs
    }
}
