package dev.xf3d3.ultimateteams.commands.subCommands;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class TeamListSubCommand {

    FileConfiguration messagesConfig = UltimateTeams.getPlugin().msgFileManager.getMessagesConfig();

    private final UltimateTeams plugin;

    public TeamListSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    public void teamListSubCommand(CommandSender sender) {
            StringBuilder teamsString = new StringBuilder();

            if (plugin.getTeamStorageUtil().getTeams().isEmpty()) {
                sender.sendMessage(Utils.Color(messagesConfig.getString("no-teams-to-list")));
            } else {
                teamsString.append(Utils.Color(messagesConfig.getString("teams-list-header") + "\n"));

                plugin.getTeamStorageUtil().getTeams().forEach(team -> {
                    String publicIndicator = team.isPublic() ?
                        Utils.Color(messagesConfig.getString("team-list-public-indicator")) :
                        Utils.Color(messagesConfig.getString("team-list-private-indicator"));
                    teamsString.append(Utils.Color(team.getName() + " " + publicIndicator + "&r\n"));
                });

                teamsString.append(" ");
                teamsString.append(Utils.Color(messagesConfig.getString("teams-list-footer")));

                sender.sendMessage(teamsString.toString());
            }
    }
}
