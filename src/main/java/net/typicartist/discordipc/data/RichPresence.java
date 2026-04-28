package net.typicartist.discordipc.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RichPresence {
    private final String state;
    private final String details;
    private final Long startTimestamp;
    private final Long endTimestamp;
    private final String largeImageKey;
    private final String largeImageText;
    private final String smallImageKey;
    private final String smallImageText;
    private final String partyId;
    private final int partySize;
    private final int partyMax;
    private final String matchSecret;
    private final String joinSecret;
    private final String spectateSecret;
    private final boolean instance;

    public RichPresence(Builder builder) {
        this.state = builder.state;
        this.details = builder.details;
        this.startTimestamp = builder.startTimestamp;
        this.endTimestamp = builder.endTimestamp;
        this.largeImageKey = builder.largeImageKey;
        this.largeImageText = builder.largeImageText;
        this.smallImageKey = builder.smallImageKey;
        this.smallImageText = builder.smallImageText;
        this.partyId = builder.partyId;
        this.partySize = builder.partySize;
        this.partyMax = builder.partyMax;
        this.matchSecret = builder.matchSecret;
        this.joinSecret = builder.joinSecret;
        this.spectateSecret = builder.spectateSecret;
        this.instance = builder.instance;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            if (state != null) json.put("state", state);
            if (details != null) json.put("details", details);
        
            if (startTimestamp != null || endTimestamp != null) {
                JSONObject timestamps = new JSONObject();
                if (startTimestamp != null) json.put("start", startTimestamp);
                if (endTimestamp != null) json.put("end", endTimestamp);
                json.put("timestamps", timestamps);
            }

            if (largeImageKey != null || smallImageKey != null) {
                JSONObject assets = new JSONObject();
                if (largeImageKey != null) assets.put("largeImageKey", largeImageKey); 
                if (largeImageText != null) assets.put("largeImageText", largeImageText); 
                if (smallImageKey != null) assets.put("smallImageKey", smallImageKey); 
                if (smallImageText != null) assets.put("smallImageText", smallImageText); 
                json.put("assets", assets);
            }

            if (partyId != null) {
                JSONObject party = new JSONObject();
                party.put("id", partyId);
                if (partySize > 0 && partyMax > 0) {
                    party.put("size", new JSONArray().put(partySize).put(partyMax));
                }
                party.put("party", party);
            }

            if (matchSecret != null || joinSecret != null || spectateSecret != null) {
                JSONObject secrets = new JSONObject();
                if (matchSecret != null) secrets.put("match", matchSecret);
                if (joinSecret!= null) secrets.put("join", joinSecret); 
                if (spectateSecret != null) secrets.put("spectate", spectateSecret);
                json.put("secrets", secrets);
            }

            json.put("instance", instance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static class Builder {
        private String state;
        private String details;
        private long startTimestamp;
        private long endTimestamp;
        private String largeImageKey;
        private String largeImageText;
        private String smallImageKey;
        private String smallImageText;
        private String partyId;
        private int partySize;
        private int partyMax;
        private String matchSecret;
        private String joinSecret;
        private String spectateSecret;
        private boolean instance;
        
        public Builder setState(String state) { this.state = state; return this; }
        public Builder setDetails(String details) { this.details = details; return this; }
        public Builder setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; return this; }
        public Builder setEndTimestamp(long endTimestamp) { this.endTimestamp = endTimestamp; return this; }
        public Builder setLargeImage(String key, String text) { 
            this.largeImageKey = key; this.largeImageText = text; return this;
        }
        public Builder setSmallImage(String key, String text) {
            this.smallImageKey = key; this.smallImageText = text; return this;
        }
        public Builder setParty(String id, int size, int max) {
            this.partyId = id; this.partySize = size; this.partyMax = max; return this;
        }
        public Builder setMatchSecret(String matchSecret) { this.matchSecret = matchSecret; return this; }
        public Builder setJoinSecret(String joinSecret) { this.joinSecret = joinSecret; return this; }
        public Builder setSpectateSecret(String spectateSecret) { this.spectateSecret = spectateSecret; return this; }
        public Builder setInstance(boolean instance) { this.instance = instance; return this; }

        public RichPresence build() {
            return new RichPresence(this);
        }
    }
}
