package dev.nedhuman.advancedantivpn.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import dev.nedhuman.advancedantivpn.AdvancedAntiVPN;
import dev.nedhuman.advancedantivpn.IPCheckerService;

public class ServerPingListener implements PacketListener {

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListener(new ServerPingListener(), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPacketType() == PacketType.Status.Server.RESPONSE || event.getPacketType() == PacketType.Status.Server.PONG) {
            String ip = event.getUser().getAddress().getAddress().getHostAddress();
            IPCheckerService.Result result = AdvancedAntiVPN.getInstance().getCheckerService().check(ip);
            if(!result.allow()) {
                event.setCancelled(true);
            }
        }
    }
}
