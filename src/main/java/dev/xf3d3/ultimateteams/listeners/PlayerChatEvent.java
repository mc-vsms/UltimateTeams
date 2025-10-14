package dev.xf3d3.ultimateteams.listeners;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.network.Message;
import dev.xf3d3.ultimateteams.network.Payload;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChatEvent implements Listener {
    private final UltimateTeams plugin;

    public PlayerChatEvent(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Handle team chat
        if (plugin.getUsersStorageUtil().getChatPlayers().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            sendToAlternateChat(player, event.getMessage());
            return;
        }

        // Format regular chat with team prefix
        plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).ifPresent(team -> {
            String prefix = team.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                String coloredPrefix = Utils.Color(prefix);

                // Get player name (nickname if EssentialsX is available)
                String playerName;
                if (plugin.getEssentialsHook() != null) {
                    playerName = plugin.getEssentialsHook().getDisplayName(player);
                } else {
                    playerName = player.getName();
                }

                // Set chat format: [prefix]<username> message
                // %1$s = player display name, %2$s = message
                event.setFormat(Utils.Color(coloredPrefix + "&f<" + playerName + "&f> %2$s"));
            }
        });
    }

    private void sendToAlternateChat(Player player, String message) {

        plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).ifPresentOrElse(
                team -> {
                    String chatSpyPrefix = plugin.getSettings().getTeamChatSpyPrefix();

                    String messageString = plugin.getSettings().getTeamChatPrefix() + " " +
                            "&d" + player.getName() + ":&r" + " " +
                            message + " ";

                    final String msg = messageString
                            .replace("%TEAM%", team.getName())
                            .replace("%PLAYER%", player.getName());

                    // Send message to team members
                    team.sendTeamMessage(Utils.Color(msg));

                    // Send spy message
                    if (plugin.getSettings().teamChatSpyEnabled()) {
                        Bukkit.broadcast(Utils.Color(chatSpyPrefix + " " + msg), "ultimateteams.chat.spy");
                    }

                    // Send globally via a message
                    plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                            .type(Message.Type.TEAM_CHAT_MESSAGE)
                            .payload(Payload.string(msg))
                            .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                            .build()
                            .send(broker, player));
                },
                () -> plugin.getUsersStorageUtil().getChatPlayers().remove(player.getUniqueId())
        );
    }
}
