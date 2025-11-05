package net.typicartist.discordipc.enums;

public enum DiscordBuild {
    STABLE("//discord.com/api"),
    CANARY("//canary.discord.com/api"),
    PTB("//ptb.discord.com/api"),
    ANY(null);

    private final String endpoint;

    DiscordBuild(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public static DiscordBuild from(String value) {
        for (DiscordBuild v : values()) {
            if (v != null && v.endpoint.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return ANY;
    }
}
