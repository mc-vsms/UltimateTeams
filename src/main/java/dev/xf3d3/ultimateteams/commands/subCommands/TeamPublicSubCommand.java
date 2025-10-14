package dev.xf3d3.ultimateteams.commands.subCommands;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamPublicSubCommand {

    private final FileConfiguration messagesConfig;
    private final UltimateTeams plugin;

    public TeamPublicSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
    }

    public void teamPublicSubCommand(CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).ifPresentOrElse(
                team -> {
                    // Check if player is team owner
                    if (!plugin.getTeamStorageUtil().isTeamOwner(player)) {
                        sender.sendMessage(Utils.Color(messagesConfig.getString("team-must-be-owner")));
                        return;
                    }

                    // Toggle public status
                    boolean newPublicStatus = !team.isPublic();
                    team.setPublic(newPublicStatus);

                    // Update team data
                    plugin.getTeamStorageUtil().updateTeamData(player, team);

                    // Send confirmation message
                    if (newPublicStatus) {
                        player.sendMessage(Utils.Color(messagesConfig.getString("team-public-enabled")));

                        // Notify team members
                        if (plugin.getSettings().teamJoinAnnounce()) {
                            team.sendTeamMessage(Utils.Color(messagesConfig.getString("team-public-enabled-broadcast")
                                    .replace("%TEAM%", team.getName())));
                        }
                    } else {
                        player.sendMessage(Utils.Color(messagesConfig.getString("team-public-disabled")));

                        // Notify team members
                        if (plugin.getSettings().teamJoinAnnounce()) {
                            team.sendTeamMessage(Utils.Color(messagesConfig.getString("team-public-disabled-broadcast")
                                    .replace("%TEAM%", team.getName())));
                        }
                    }
                },
                () -> sender.sendMessage(Utils.Color(messagesConfig.getString("not-in-team")))
        );
    }
}
