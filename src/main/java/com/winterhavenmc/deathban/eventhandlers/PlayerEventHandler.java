/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathban.eventhandlers;

import com.winterhavenmc.deathban.PluginMain;
import com.winterhavenmc.deathban.messages.Macro;
import com.winterhavenmc.deathban.messages.MessageId;
import com.winterhavenmc.deathban.tasks.KickPlayerTask;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.winterhavenmc.util.TimeUnit.MINUTES;
import static com.winterhavenmc.util.TimeUnit.SECONDS;


/**
 * Class that implements event handlers for player related events
 */
public class PlayerEventHandler implements Listener {

	// reference to plugin main class
	private final PluginMain plugin;

	// set of player uuids that will be kicked on respawn
	private final Set<UUID> kickSet = ConcurrentHashMap.newKeySet();


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public PlayerEventHandler(final PluginMain plugin) {

		// set reference to plugin main class
		this.plugin = plugin;

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Event handler for PlayerDeathEvent
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {

		// get event player
		Player player = event.getEntity();

		// if player has exempt permission, do nothing and return
		if (player.hasPermission("deathban.exempt")) {
			return;
		}

		// if world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}

		// ban player
		banPlayer(player);

		// ban player ip if configured
		if (plugin.getConfig().getBoolean("ban-ip")) {
			banPlayerIp(player);
		}

		// put player in kick on respawn set
		kickSet.add(player.getUniqueId());
	}


	/**
	 * Event handler for PlayerRespawnEvent
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event) {

		// get event player
		Player player = event.getPlayer();

		// if player is in kick on respawn set, kick after configured delay
		if (kickSet.contains(player.getUniqueId())) {
			new KickPlayerTask(plugin, player).runTaskLater(plugin, SECONDS.toTicks(plugin.getConfig().getLong("kick-delay")));

			// remove player from kick on respawn set
			kickSet.remove(player.getUniqueId());
		}
	}


	/**
	 * Add player to ban list
	 *
	 * @param player the player to add to ban list
	 */
	private void banPlayer(final Player player) {

		// get ban list
		BanList banList = plugin.getServer().getBanList(BanList.Type.NAME);

		// get ban message from language file
		String banMessage = plugin.messageBuilder.compose(player, MessageId.ACTION_PLAYER_BAN).toString();

		// get ban reason string from config file
		String banReason = plugin.getConfig().getString("ban-reason");

		// add player to ban list
		BanEntry banEntry = banList.addBan(player.getName(), banMessage, getExpireDate(), banReason);

		// save ban entry
		if (banEntry != null) {
			banEntry.save();

			// write log entry if configured
			if (plugin.getConfig().getBoolean("log-bans")) {
				// get log message from language file
				String logMessage = plugin.messageBuilder.compose(player, MessageId.LOG_PLAYER_BAN)
						.setMacro(Macro.DURATION, MINUTES.toMillis(plugin.getConfig().getLong("ban-time")))
						.toString();

				// log message
				plugin.getLogger().info(logMessage);
			}
		}
	}


	/**
	 * Add player IP address to ban list
	 *
	 * @param player the player whose IP address to add to ban list
	 */
	private void banPlayerIp(final Player player) {

		// get player ip
		InetSocketAddress playerAddress = player.getAddress();

		// if player address is null, do nothing and return
		if (playerAddress == null) {
			return;
		}

		// get ip ban list
		BanList ipBanList = plugin.getServer().getBanList(BanList.Type.IP);

		// get ban message from language file
		String message = plugin.messageBuilder.compose(player, MessageId.ACTION_PLAYER_BAN)
				.setMacro(Macro.DURATION, MINUTES.toMillis(plugin.getConfig().getLong("ban-time")))
				.toString();

		// get ban reason string from config file
		String banReason = plugin.getConfig().getString("ban-reason");

		// add player ip to ban list
		BanEntry ipBanEntry = ipBanList.addBan(playerAddress.getHostString(), message, getExpireDate(), banReason);

		// save ban entry
		if (ipBanEntry != null) {
			ipBanEntry.save();

			// write log entry if configured
			if (plugin.getConfig().getBoolean("log-bans")) {
				// get log message from language file
				String logMessage = plugin.messageBuilder.compose(player, MessageId.LOG_PLAYER_IP_BAN)
						.setMacro(Macro.PLAYER_IP, playerAddress.getHostString())
						.toString();

				// log message
				plugin.getLogger().info(logMessage);
			}
		}
	}


	/**
	 * Get expire date from configured ban time added to current time
	 *
	 * @return Date object representing expire time, or null if configured ban time is negative
	 */
	private Date getExpireDate() {

		// if configured ban time is negative, return null for permanent ban
		if (plugin.getConfig().getLong("ban-time") < 0) {
			return null;
		}

		// return expire time date object
		return new Date(System.currentTimeMillis() + MINUTES.toMillis(plugin.getConfig().getLong("ban-time")));
	}

}
