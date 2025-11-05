package net.typicartist.discordipc.data;

import org.json.JSONException;
import org.json.JSONObject;

public class RichPresense {
    private String state = "OK";
    private String details = "SHINE";

    private Timestamps timestamp;
    private Assets assets;
    private Party party;
    private Secrets secrets;

    private int instance;

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (state != null) json.put("state", state);
        if (details != null) json.put("details", details);

        return json;
    }

    private static class Assets {
        String largeImageKey;
        String largeImageText;
        String smallImageKey;
        String smallImageText;
    }
    
    private static class Party {
        String id;
        int size;
        int max;
        int privacy;
    }


    private static class Timestamps {
        long start;
        long end;
    }

    private static class Secrets {
        String match;
        String join;
        String spectate;
    }
}
