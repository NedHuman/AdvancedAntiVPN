package dev.nedhuman.advancedantivpn.listener;

import dev.nedhuman.advancedantivpn.AdvancedAntiVPN;
import dev.nedhuman.advancedantivpn.IPCheckerService;
import dev.nedhuman.advancedantivpn.Utils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String ip = event.getAddress().getHostAddress();
        Player player = event.getPlayer();

        if(Utils.bypassesAntiVPN(player)) {
            return;
        }

        IPCheckerService.Result result = AdvancedAntiVPN.getInstance().getCheckerService().check(ip);
        if(!result.allow()) {
            if(AdvancedAntiVPN.getInstance().isNotifyChat()) {
                AdvancedAntiVPN.getInstance().getLogger().info(player.getName()+" attempted to connect with a forbidden VPN");

                TextComponent message = Utils.getMessage(player, result);

                for(Player i : Bukkit.getOnlinePlayers()) {
                    if(i.hasPermission("advancedantivpn.notify")) {
                        i.spigot().sendMessage(message);
                    }
                }
            }
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED+"You may not connect to this server using a VPN");
        }
    }
}
