package dev.xf3d3.ultimateteams.listeners;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.network.Message;
import dev.xf3d3.ultimateteams.network.Payload;
import dev.xf3d3.ultimateteams.utils.Utils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class PlayerChatEvent implements Listener {
    private final UltimateTeams plugin;

    public PlayerChatEvent(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Handle team chat
        if (plugin.getUsersStorageUtil().getChatPlayers().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            // Convert Component to String for team chat
            String message = LegacyComponentSerializer.legacySection().serialize(event.message());
            sendToAlternateChat(player, message);
            return;
        }

        // Use renderer to customize chat format
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            // Get player name (nickname if EssentialsX is available)
            String displayName;
            if (plugin.getEssentialsHook() != null) {
                displayName = plugin.getEssentialsHook().getDisplayName(source);
            } else {
                displayName = source.getName();
            }

            // Get real player name
            String realName = source.getName();

            // Start building the message
            Component result = Component.empty();

            // Check if player is in a team and add prefix if available
            var teamOptional = plugin.getTeamStorageUtil().findTeamByMember(source.getUniqueId());
            if (teamOptional.isPresent()) {
                String prefix = teamOptional.get().getPrefix();
                if (prefix != null && !prefix.isEmpty()) {
                    String coloredPrefix = Utils.Color(prefix);
                    // Add prefix with legacy colors
                    LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                    Component prefixComponent = serializer.deserialize(coloredPrefix);
                    result = result.append(prefixComponent);
                }
            }

            // Add player name with hover event
            Component playerNameComponent = text("<" + displayName + ">")
                    .hoverEvent(HoverEvent.showText(text(realName)));

            // Combine: prefix + playerName + space + message
            return result.append(playerNameComponent).append(text(" ")).append(message);
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
