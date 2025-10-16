package dev.xf3d3.ultimateteams.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import dev.xf3d3.ultimateteams.UltimateTeams;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EssentialsHook {

    private final Essentials essentials;
    private final UltimateTeams plugin;

    public EssentialsHook(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");

        if (this.essentials == null) {
            throw new IllegalStateException("Could not find Essentials plugin");
        }

        sendMessages();
    }

    /**
     * Gets the formatted nickname for a player from EssentialsX
     * If the player has no nickname, returns null
     *
     * @param player The player to get the nickname for
     * @return The formatted nickname with colors, or null if no nickname is set
     */
    @Nullable
    public String getFormattedNickname(@NotNull Player player) {
        try {
            User user = essentials.getUser(player);
            if (user != null && user.getNick(true, true) != null) {
                return user.getNick(true, true);
            }
        } catch (Exception e) {
            plugin.log(java.util.logging.Level.WARNING,
                    "Failed to get nickname for player " + player.getName(), e);
        }
        return null;
    }

    /**
     * Gets the display name for a player
     * Returns the formatted nickname if available, otherwise returns the player name
     *
     * @param player The player to get the display name for
     * @return The display name (nickname or player name)
     */
    @NotNull
    public String getDisplayName(@NotNull Player player) {
        String nickname = getFormattedNickname(player);
        return nickname != null ? nickname : player.getName();
    }

    /**
     * Checks if a player has a nickname set
     *
     * @param player The player to check
     * @return true if the player has a nickname, false otherwise
     */
    public boolean hasNickname(@NotNull Player player) {
        try {
            User user = essentials.getUser(player);
            return user != null && user.getNickname() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendMessages() {
        plugin.sendConsole("-------------------------------------------");
        plugin.sendConsole("&6UltimateTeams: &3Hooked into EssentialsX");
        plugin.sendConsole("&6UltimateTeams: &3Nickname support enabled!");
        plugin.sendConsole("-------------------------------------------");
    }

    public Essentials getEssentials() {
        return essentials;
    }
}
