package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Msg {

    private final JSONObject values;

    public Msg(JSONObject values) {
        this.values = values;
    }

    public String getText() throws JSONException {
        return values.getString(Variable.text.name());
    }

    public User getUser() throws JSONException {
        return new User(values.getJSONObject(Variable.user.name()));
    }

    public Meta getMeta() throws JSONException {
        return new Meta(values.getJSONObject(Variable.meta.name()));
    }

    public enum Variable {
        text, user, meta
    }

    public static class User {

        private final JSONObject values;

        public User(JSONObject values) {
            this.values = values;
        }

        public String getUserName() throws JSONException {
            return values.getString(Variable.username.name());
        }

        public int getUid() throws JSONException {
            return values.getInt(Variable.uid.name());
        }

        public int getRank() throws JSONException {
            return values.getInt(Variable.rank.name());
        }

        public int getBadge() throws JSONException {
            return values.getInt(Variable.badge.name());
        }

        public int getLevel() throws JSONException {
            return values.getInt(Variable.level.name());
        }

        public enum Variable {
            username, uid, rank, badge, level
        }
    }

    public static class Meta {

        private final JSONObject values;

        public Meta(JSONObject values) {
            this.values = values;
        }

        public int getRank() throws JSONException {
            return values.getInt(Variable.rank.name());
        }

        public int getCid() throws JSONException {
            return values.getInt(Variable.cid.name());
        }

        public enum Variable {
            rank, cid
        }
    }
}
