package dev.nedhuman.advancedantivpn;

import dev.nedhuman.advancedantivpn.listener.PlayerPreJoinListener;
import dev.nedhuman.advancedantivpn.listener.ServerPingListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class AdvancedAntiVPN extends JavaPlugin {

    private static AdvancedAntiVPN instance;

    private boolean notifyChat;

    private IPCheckerService checkerService;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        boolean blockDataCenter = getConfig().getBoolean("block-data-center", true);
        boolean blockVpn = getConfig().getBoolean("block-vpn", true);
        boolean blockProxy = getConfig().getBoolean("block-proxy", true);
        boolean letinDuringError = getConfig().getBoolean("let-in-during-exception", true);
        List<String> additionalIps = getConfig().getStringList("blocked-ips");
        List<String> whitelistedIps = getConfig().getStringList("whitelisted-ips");

        boolean blockPing = getConfig().getBoolean("block-ping");
        notifyChat = getConfig().getBoolean("notify-chat", true);

        checkerService = new IPCheckerService()
                .setBlockVpn(blockVpn)
                .setBlockDataCenter(blockDataCenter)
                .setBlockProxy(blockProxy)
                .setLetInDuringError(letinDuringError);
        checkerService.getBlockedIpCache().addAll(additionalIps);
        checkerService.getGoodIpCache().addAll(whitelistedIps);

        getServer().getPluginManager().registerEvents(new PlayerPreJoinListener(), this);

        if(blockPing) {
            if(getServer().getPluginManager().getPlugin("packetevents") != null) {
                ServerPingListener.init();
            } else {
                getLogger().warning("It seems block-ping has been set to true in the config, however PacketEvents is not installed. Ignoring.");
            }
        }
    }

    public IPCheckerService getCheckerService() {
        return checkerService;
    }

    public boolean isNotifyChat() {
        return notifyChat;
    }

    @Override
    public void onDisable() {

    }

    public static AdvancedAntiVPN getInstance() {
        return instance;
    }
}
