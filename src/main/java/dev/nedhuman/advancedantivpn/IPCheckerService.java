package dev.nedhuman.advancedantivpn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class for checking blocked IPs
 */
public final class IPCheckerService {

    public static final String IP_URL = "https://api.iprisk.info/v1/";

    private Set<String> blockedIpCache;
    private Set<String> goodIpCache;

    private boolean blockVpn;
    private boolean blockProxy;
    private boolean blockDataCenter;
    private boolean letInDuringError;

    public IPCheckerService() {
        blockedIpCache = new HashSet<>();
        goodIpCache = new HashSet<>();
    }

    public IPCheckerService setBlockVpn(boolean blockVpn) {
        this.blockVpn = blockVpn;
        return this;
    }

    public IPCheckerService setBlockProxy(boolean blockProxy) {
        this.blockProxy = blockProxy;
        return this;
    }

    public IPCheckerService setBlockDataCenter(boolean blockDataCenter) {
        this.blockDataCenter = blockDataCenter;
        return this;
    }

    public IPCheckerService setLetInDuringError(boolean letInDuringError) {
        this.letInDuringError = letInDuringError;
        return this;
    }

    public Result check(String ip) {

        // First check the cache
        if(blockedIpCache.contains(ip)) {
            return Result.newBad("Address found in blocked IP cache", null);
        }
        if(goodIpCache.contains(ip)) {
            return Result.newGood();
        }
        try {
            URL url = new URL(IP_URL+ip);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input = in.readLine();

            in.close();
            connection.disconnect();

            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            if(json == null) {
                throw new IllegalArgumentException("Unable to parse JSON from connection");
            }

            if(json.has("error")) {
                throw new IllegalArgumentException("Error from API: "+json.get("error").getAsJsonObject().get("message").getAsString());
            }

            boolean dataCenter = json.get("data_center").getAsBoolean();
            boolean openProxy = json.get("open_proxy").getAsBoolean();
            boolean vpn = json.get("vpn").getAsBoolean();

            String isp = json.get("network").getAsJsonObject().get("as_org").getAsString();

            if((dataCenter && blockDataCenter) || (openProxy && blockProxy) || (vpn && blockVpn)) {
                blockedIpCache.add(ip);
                return Result.newBad("User attempted to connect with a VPN", isp);
            }else{
                goodIpCache.add(ip);
            }


        } catch(Exception e) {
            AdvancedAntiVPN.getInstance().getLogger().severe("Unhandled "+e.getClass().getName()+" occured while processing check for IP "+ip);
            e.printStackTrace();
            if(!letInDuringError) {
                return Result.newBad("Unhandled exception, see console for details", null);
            }
        }

        return Result.newGood();
    }

    public record Result(
            boolean allow,
            String message,
            String isp
    ) {
        public static Result newGood() {
            return new Result(true, "", null);
        }

        public static Result newBad(String reason, String isp) {
            return new Result(false, reason, isp);
        }
    }

    public Set<String> getBlockedIpCache() {
        return blockedIpCache;
    }
}
