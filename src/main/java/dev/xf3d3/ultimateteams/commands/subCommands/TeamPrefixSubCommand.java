package dev.xf3d3.ultimateteams.commands.subCommands;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamPrefixSubCommand {

    private final int MIN_CHAR_LIMIT;
    private final int MAX_CHAR_LIMIT;
    private final FileConfiguration messagesConfig;

    private final UltimateTeams plugin;

    public TeamPrefixSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
        this.MAX_CHAR_LIMIT = plugin.getSettings().getTeamTagsMaxCharLimit();
        this.MIN_CHAR_LIMIT = plugin.getSettings().getTeamTagsMinCharLimit();
    }

    public void teamPrefixSubCommand(CommandSender sender, String prefix, List<String> bannedTags) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        if (bannedTags.stream().map(String::toLowerCase).toList().contains(prefix.toLowerCase())) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-is-banned").replace("%TEAMPREFIX%", prefix)));
            return;
        }

        if (plugin.getTeamStorageUtil().getTeams().stream().map(Team::getPrefix).toList().contains(prefix)) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-already-taken").replace("%TEAMPREFIX%", prefix)));
            return;
        }

        if (!plugin.getSettings().isTeamTagAllowColorCodes() && (prefix.contains("&") || prefix.contains("#"))) {

            player.sendMessage(Utils.Color(messagesConfig.getString("team-tag-cannot-contain-colours")));
            return;
        }

        if (plugin.getSettings().isTeamTagRequirePermColorCodes() && !player.hasPermission("ultimateteams.team.tag.usecolors") && (prefix.contains("&") || prefix.contains("#"))) {

            player.sendMessage(Utils.Color(messagesConfig.getString("use-colours-missing-permission")));
            return;
        }

        final int prefixLength = Utils.removeColors(prefix).length();
        if (prefixLength >= MIN_CHAR_LIMIT && prefixLength <= MAX_CHAR_LIMIT) {

            plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).ifPresentOrElse(
                    team -> {
                        // Check permission
                        if (!(plugin.getTeamStorageUtil().isTeamOwner(player) || (plugin.getTeamStorageUtil().isTeamManager(player) && team.hasPermission(Team.Permission.PREFIX)))) {
                            sender.sendMessage(Utils.Color(messagesConfig.getString("no-permission")));
                            return;
                        }

                        team.setPrefix(prefix);
                        plugin.runAsync(task -> plugin.getTeamStorageUtil().updateTeamData(player, team));

                        sender.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-change-successful")).replace("%TEAMPREFIX%", prefix));

                        // Update tab list for all team members (scheduled internally for Folia compatibility)
                        plugin.getTabListManager().updateTeamTabList(team);
                    },
                    () -> sender.sendMessage(Utils.Color(messagesConfig.getString("not-in-team")))
            );

        } else if (prefixLength > MAX_CHAR_LIMIT) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-too-long").replace("%CHARMAX%", String.valueOf(MAX_CHAR_LIMIT))));
        } else {
            sender.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-too-short").replace("%CHARMIN%", String.valueOf(MIN_CHAR_LIMIT))));
        }
    }
}
