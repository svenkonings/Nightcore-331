package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WaitlistUpdate {

    private final JSONObject values;

    public WaitlistUpdate(JSONObject vlaues) {
        this.values = vlaues;
    }

    public Waitlist getWaitlist() throws JSONException {
        return new Waitlist(values.getJSONArray(Variable.waitlist.name()));
    }

    public enum Variable {
        waitlist
    }

    public static class Waitlist {

        private final JSONArray values;

        public Waitlist(JSONArray values) {
            this.values = values;
        }

        public Song getSong(int index) throws JSONException {
            return new Song(values.getJSONObject(index));
        }

        public int length() {
            return values.length();
        }
    }
}
