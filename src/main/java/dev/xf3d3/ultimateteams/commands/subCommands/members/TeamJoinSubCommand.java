package dev.xf3d3.ultimateteams.commands.subCommands.members;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamJoinSubCommand {

    private final FileConfiguration messagesConfig;
    private final UltimateTeams plugin;
    public static final String PLAYER_PLACEHOLDER = "%PLAYER%";
    public static final String TEAM_PLACEHOLDER = "%TEAM%";

    public TeamJoinSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
    }

    public void teamJoinSubCommand(CommandSender sender, String teamName) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        // Check if player is already in a team
        if (plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).isPresent()) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-join-failed-already-in-team")));
            return;
        }

        // Find the team by name
        plugin.getTeamStorageUtil().findTeamByName(teamName).ifPresentOrElse(
                team -> {
                    // Check if the team is public
                    if (!team.isPublic()) {
                        player.sendMessage(Utils.Color(messagesConfig.getString("team-join-failed-not-public")));
                        return;
                    }

                    // Check max members limit
                    plugin.getUsersStorageUtil().getPlayer(player.getUniqueId()).thenAccept(teamPlayer -> {
                        // Get team owner to check their max members permission
                        plugin.getUsersStorageUtil().getPlayer(team.getOwner()).thenAccept(ownerPlayer -> {
                            Player ownerOnline = plugin.getServer().getPlayer(team.getOwner());
                            int maxMembers = ownerPlayer.getMaxMembers(
                                    ownerOnline,
                                    plugin.getSettings().getTeamMaxSize(),
                                    plugin.getSettings().getStackedTeamSize()
                            );

                            if (team.getMembers().size() >= maxMembers) {
                                player.sendMessage(Utils.Color(messagesConfig.getString("team-join-failed-max-size-reached"))
                                        .replace("%LIMIT%", String.valueOf(maxMembers)));
                                return;
                            }

                            // Add player to team
                            plugin.getTeamStorageUtil().addTeamMember(team, player);

                            // Send confirmation message to joining player
                            String joinMessage = Utils.Color(messagesConfig.getString("team-join-successful"))
                                    .replace(TEAM_PLACEHOLDER, team.getName());
                            player.sendMessage(joinMessage);

                            // Announce to team members if enabled
                            if (plugin.getSettings().teamJoinAnnounce()) {
                                team.sendTeamMessage(Utils.Color(messagesConfig.getString("team-join-broadcast-chat")
                                        .replace(PLAYER_PLACEHOLDER, player.getName())
                                        .replace(TEAM_PLACEHOLDER, Utils.Color(team.getName()))));
                            }
                        });
                    });
                },
                () -> player.sendMessage(Utils.Color(messagesConfig.getString("team-join-failed-team-not-found")))
        );
    }
}
