package net.ddns.meteoorkip.nightcore_331.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAdd {

    private final JSONObject values;

    public UserAdd(JSONObject values) {
        this.values = values;
    }

    public User getUser() throws JSONException {
        return new User(values.getJSONObject(Variables.user.name()));
    }

    public enum Variables {
        user
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
}
