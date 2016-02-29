package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Song {

    private final JSONObject values;

    public Song(JSONObject values) {
        this.values = values;
    }

    public User getUser() throws JSONException {
        return new User(values.getJSONObject(Variable.user.name()));
    }

    public Data getData() throws JSONException {
        return new Data(values.getJSONObject(Variable.data.name()));
    }

    public boolean hasIdHistory() {
        return values.has(Variable.Optional.idhistory.name());
    }

    public int optIdHistory() {
        return values.optInt(Variable.Optional.idhistory.name());
    }

    public enum Variable {
        user, data;

        public enum Optional {
            idhistory
        }
    }

    public static class User {

        private final JSONObject values;

        public User(JSONObject values) {
            this.values = values;
        }

        public int getUserId() throws JSONException {
            return values.getInt(Variable.userid.name());
        }

        public String getUserName() throws JSONException {
            return values.getString(Variable.username.name());
        }

        public int getRank() throws JSONException {
            return values.getInt(Variable.rank.name());
        }

        public String getUncd() throws JSONException {
            return values.getString(Variable.uncd.name());
        }

        public Profile getProfile() throws JSONException {
            return new Profile(values.getJSONObject(Variable.profile.name()));
        }

        public enum Variable {
            userid, username, rank, uncd, profile
        }

        public static class Profile {

            private final JSONObject values;

            public Profile(JSONObject values) {
                this.values = values;
            }

            public int getBadgeId() throws JSONException {
                return values.getInt(Variable.badge_id.name());
            }

            public long getExperience() throws JSONException {
                return values.getLong(Variable.experience.name());
            }

            public enum Variable {
                badge_id, experience
            }
        }
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

        public boolean hasRem() {
            return values.has(Variable.Optional.rem.name());
        }

        public long optRem() {
            return values.optLong(Variable.Optional.rem.name());
        }

        public enum Variable {
            cid, dur, title, thumbnail;

            public enum Optional {
                rem
            }
        }
    }
}
