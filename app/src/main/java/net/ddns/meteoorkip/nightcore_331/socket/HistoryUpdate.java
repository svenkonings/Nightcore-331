package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryUpdate {

    private final JSONObject values;

    public HistoryUpdate(JSONObject values) {
        this.values = values;
    }

    public Data getData() throws JSONException {
        return new Data(values.getJSONObject(Variable.data.name()));
    }

    public enum Variable {
        data
    }

    public static class Data {

        private final JSONObject values;

        public Data(JSONObject values) {
            this.values = values;
        }

        public String getCid() throws JSONException {
            return values.getString(Variable.cid.name());
        }

        public long getDur() throws JSONException {
            return values.getLong(Variable.dur.name());
        }

        public String getTitle() throws JSONException {
            return values.getString(Variable.title.name());
        }

        public String getThumbnail() throws JSONException {
            return values.getString(Variable.thumbnail.name());
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

        public int getUserCount() throws JSONException {
            return values.getInt(Variable.usercount.name());
        }

        public enum Variable {
            cid, dur, title, thumbnail, woots, mehs, grabs, usercount
        }
    }
}
