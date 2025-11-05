package net.typicartist.discordipc.data;

import org.json.JSONObject;

public class User {
    public final String id;
    public final String username;
    public final String discriminator;
    public final String avatar;

    public User(String id, String username, String discriminator, String avatar) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getAvatar() {
        return avatar;
    }

    public static User from(JSONObject data) {
        return new User(
            data.optString("id", null),
            data.optString("username", null),
            data.optString("discriminator", null),
            data.optString("avatar", null)
        );
    }

    public static class Avatar {
        private final String id;
        private final String hash;
        
        private Avatar(String id, String hash) {
            this.id = id;
            this.hash = hash;
        }

        public String getHash() {
            return hash;
        }

        public String getUrl() {
            return null;
        }

        private enum DefaultAvatar {
            A("");


            private final String hash;

            DefaultAvatar(String hash) {
                this.hash = hash;
            }
        }
    }
}
