package net.typicartist.discordipc.data;

import org.json.JSONObject;

public class User {
    private final String id;
    private final String username;
    private final String discriminator;
    private final Avatar avatar;

    public User(String id, String username, String discriminator, String hash) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = new Avatar(id, hash);
    }

    public static User fromJson(JSONObject data) {
        return new User(
            data.optString("id", null),
            data.optString("username", null),
            data.optString("discri2minator", null),
            data.optString("avatar", null)
        );
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Deprecated
    public String getDiscriminator() {
        return discriminator;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public static class Avatar {
        private static final String CDN_BASE = "https://cdn.discordapp.com";
        private static final String AVATAR_URL = CDN_BASE + "/avatars/%s/%s.%s";
        private static final String DEFAULT_AVATAR_URL = CDN_BASE + "/embed/avatars/%d.png";

        private final String userId;
        private final String hash;
        
        private Avatar(String userId, String hash) {
            this.userId = userId;
            this.hash = hash;
        }

        public String getHash() {
            return hash;
        }

        public boolean isDefault() {
            return hash.equals("null");
        }

        public String getUrl() {
            return getUrl(ImageFormat.PNG);
        }

        public String getUrl(ImageFormat format) {
            if (isDefault()) {
                long id = Long.parseLong(userId);
                return String.format(DEFAULT_AVATAR_URL, (id >> 22) % 5);
            }

            ImageFormat actual = (format == ImageFormat.GIF && !hash.startsWith("a_"))
                ? ImageFormat.PNG
                : format;
            return String.format(AVATAR_URL, userId, hash, actual.getExtension());
        }

        public enum ImageFormat {
            PNG("png"),
            JPEG("jpeg"),
            WEBP("webp"),
            GIF("gif");

            private final String extension;

            ImageFormat(String extension) {
                this.extension = extension;
            }

            public String getExtension() {
                return extension;
            }
        }
    }
}
