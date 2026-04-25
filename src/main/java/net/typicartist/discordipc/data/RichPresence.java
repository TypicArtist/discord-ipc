package net.typicartist.discordipc.data;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.enums.PartyPrivacy;

public class RichPresence {
    private String state;
    private String details;
    private Timestamps timestamps;
    private Assets assets;
    private Party party;
    private Secrets secrets;
    private int instance;

    public RichPresence setState(String state) {
        this.state = state;
        return this;
    }

    public RichPresence setDetails(String details) {
        this.details = details;
        return this;
    }

    public RichPresence setTimestamps(Timestamps timestamps) {
        this.timestamps = timestamps;
        return this;
    }

    public RichPresence setParty(Party party) {
        this.party = party;
        return this;
    }

    public RichPresence setSecrets(Secrets secrets) {
        this.secrets = secrets;
        return this;
    }

    public RichPresence setInstance(int instance) {
        this.instance = instance;
        return this;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        if (state != null) json.put("state", state);
        if (details != null) json.put("details", details);

        if (timestamps != null) {
            if (timestamps.start > 0) json.put("startTimestamp", timestamps.start);
            if (timestamps.end > 0) json.put("endTimestamp", timestamps.end);
        }

        if (assets != null) {
            if (assets.largeImageKey != null) json.put("largeImageKey", assets.largeImageKey); 
            if (assets.largeImageText != null) json.put("largeImageText", assets.largeImageText); 
            if (assets.smallImageKey != null) json.put("smallImageKey", assets.smallImageKey); 
            if (assets.smallImageText != null) json.put("smallImageText", assets.smallImageText); 
        }

        if (party != null) {
            if (party.id != null) json.put("partyId", party.id);
            if (party.size > 0) json.put("partySize", party.size);
            if (party.max > 0) json.put("partyMax", party.max);
            json.put("partyPrivacy", party.privacy.ordinal());
        }

        if (secrets != null) {
            if (secrets.match != null) json.put("matchSecret", secrets.match);
            if (secrets.join != null) json.put("joinSecret", secrets.join); 
            if (secrets.spectate != null) json.put("spectateSecret", secrets.spectate); 
        }

        if (instance > 0) json.put("instance", instance);

        return json;
    }

    public static class Assets {
        private String largeImageKey;
        private String largeImageText;
        private String smallImageKey;
        private String smallImageText;

        public Assets setLargeImageKey(String largeImageKey) {
            this.largeImageKey = largeImageKey;
            return this;
        }

        public Assets setLargeImageText(String largeImageText) {
            this.largeImageText = largeImageText;
            return this;
        }

        public Assets setSmallimageKey(String smallImageKey) {
            this.smallImageKey = smallImageKey;
            return this;
        }

        public Assets setSmallmageText(String smallImageText) {
            this.smallImageText = smallImageText;
            return this;
        }
    }
    
    public static class Party {
        private String id;
        private int size;
        private int max;
        private PartyPrivacy privacy;

        public Party setId(String id) {
            this.id = id;
            return this;
        }

        public Party setSize(int size) {
            this.size = size;
            return this;
        }

        public Party setMax(int max) {
            this.max = max;
            return this;
        }

        public Party setPrivacy(PartyPrivacy privacy) {
            this.privacy = privacy;
            return this;
        }
    }


    public static class Timestamps {
        private long start;
        private long end;

        public Timestamps setStart(long start) {
            this.start = start;
            return this;
        }

        public Timestamps setEnd(long end) {
            this.end = end;
            return this;
        }
    }

    public static class Secrets {
        private String match;
        private String join;
        private String spectate;

        public Secrets setMatch(String match) {
            this.match = match;
            return this;
        }

        public Secrets setJoin(String join) {
            this.join = join;
            return this;
        }

        public Secrets setSpectate(String spectate) {
            this.spectate = spectate;
            return this;
        }
    }
}
