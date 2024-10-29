package dev.nedhuman.advancedantivpn;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {

    private Utils() {}

    public static TextComponent getMessage(String player, IPCheckerService.Result result) {
        TextComponent message = new TextComponent(ChatColor.DARK_RED+"[!] "+ChatColor.RED+ player+" attempted to connect with a forbidden VPN");
        String isp = "";
        if(result.isp() != null) {
            isp = ChatColor.YELLOW+"\nISP: "+ChatColor.GOLD+ result.isp();
        }
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] { new TextComponent(
                ChatColor.YELLOW+"Message: "+ChatColor.GOLD+ result.message()+isp)
        } ));
        return message;
    }
}
