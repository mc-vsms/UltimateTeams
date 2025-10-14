package dev.xf3d3.ultimateteams.utils;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TabListManager {

    private final UltimateTeams plugin;

    public TabListManager(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    /**
     * Updates the tab list prefix for a specific player
     * Uses Bukkit's setPlayerListName which works on Folia
     * @param player The player to update
     */
    public void updatePlayerTabList(@NotNull Player player) {
        plugin.getScheduler().runNextTick(task -> {
            Optional<Team> teamOpt = plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId());

            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                String prefix = team.getPrefix();

                if (prefix != null && !prefix.isEmpty()) {
                    String coloredPrefix = Utils.Color(prefix);
                    updatePlayerListName(player, coloredPrefix);
                } else {
                    updatePlayerListName(player, "");
                }
            } else {
                updatePlayerListName(player, "");
            }
        });
    }

    /**
     * Updates the tab list for all members of a team
     * @param team The team whose members' tab lists should be updated
     */
    public void updateTeamTabList(@NotNull Team team) {
        team.getOnlineMembers().forEach(this::updatePlayerTabList);
    }

    /**
     * Updates the tab list for all online players
     */
    public void updateAllTabLists() {
        Bukkit.getOnlinePlayers().forEach(this::updatePlayerTabList);
    }

    /**
     * Updates a player's display name in the player list
     * Uses Bukkit's setPlayerListName which works on Folia
     * If EssentialsX is installed, uses the player's nickname instead of their username
     * @param player The player to update
     * @param prefix The prefix to add before the player name
     */
    private void updatePlayerListName(@NotNull Player player, @NotNull String prefix) {
        try {
            // Get the player's display name (nickname from EssentialsX if available)
            String playerName;
            if (plugin.getEssentialsHook() != null) {
                playerName = plugin.getEssentialsHook().getDisplayName(player);
            } else {
                playerName = player.getName();
            }

            // Create the display name with prefix (no space between)
            String displayName = prefix + playerName;

            // Use Bukkit's native method to set player list name
            // This works on Folia without requiring Scoreboard
            player.setPlayerListName(displayName);
        } catch (Exception e) {
            plugin.log(java.util.logging.Level.WARNING,
                    "Failed to update tab list for player " + player.getName(), e);
        }
    }

    /**
     * Resets the player list name when they disconnect
     * This should be called from the PlayerQuitEvent
     * @param player The player who is disconnecting
     */
    public void cleanupPlayerTeam(@NotNull Player player) {
        try {
            player.setPlayerListName(null); // Reset to default
        } catch (Exception e) {
            // Ignore exceptions during disconnect
        }
    }
}
