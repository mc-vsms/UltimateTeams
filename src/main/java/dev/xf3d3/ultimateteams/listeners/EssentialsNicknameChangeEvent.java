package dev.xf3d3.ultimateteams.listeners;

import dev.xf3d3.ultimateteams.UltimateTeams;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class EssentialsNicknameChangeEvent implements Listener {

    private final UltimateTeams plugin;

    public EssentialsNicknameChangeEvent(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNicknameChange(NickChangeEvent event) {
        // Update tab list when nickname changes
        if (event.getAffected().getBase() != null) {
            plugin.runSyncDelayed(() ->
                plugin.getTabListManager().updatePlayerTabList(event.getAffected().getBase()), 10L
            );
        }
    }
}
