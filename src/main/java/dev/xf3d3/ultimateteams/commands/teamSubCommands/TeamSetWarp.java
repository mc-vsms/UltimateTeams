package dev.xf3d3.ultimateteams.commands.teamSubCommands;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.team.Warp;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class TeamSetWarp {
    private final FileConfiguration messagesConfig;
    private final UltimateTeams plugin;

    public TeamSetWarp(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
    }

    public void setWarpCommand(CommandSender sender, String name) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        if (!plugin.getManager().teams().isTeamOwner(player)) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-must-be-owner")));
            return;
        }

        if (!plugin.getSettings().teamWarpEnabled()) {
            player.sendMessage(Utils.Color(messagesConfig.getString("function-disabled")));
            return;
        }

        plugin.getManager().teams().findTeamByOwner(player).ifPresentOrElse(
                team -> {
                    final Collection<Warp> warps = team.getTeamWarps();

                    if (warps != null && warps.size() >= plugin.getSettings().getTeamWarpLimit()) {
                        player.sendMessage(Utils.Color(messagesConfig.getString("team-warp-limit-reached")));
                        return;
                    }


                    if (team.getTeamWarp(name) != null) {
                        player.sendMessage(Utils.Color(messagesConfig.getString("team-warp-name-used")));
                        return;
                    }

                    final Warp warp = new Warp(
                            name,
                            plugin.getServerName(),
                            Objects.requireNonNull(player.getLocation().getWorld()).getName(),
                            player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ(),
                            player.getLocation().getYaw(),
                            player.getLocation().getPitch()
                    );

                    plugin.getManager().teams().addWarp(player, team, warp);

                    player.sendMessage(Utils.Color(messagesConfig.getString("team-warp-successful").replaceAll("%WARP_NAME%", warp.getName())));
                },

                () -> player.sendMessage(Utils.Color(messagesConfig.getString("failed-not-in-team")))
        );

    }
}
