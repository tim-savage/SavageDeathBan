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

package com.winterhavenmc.deathban.commands;

import com.winterhavenmc.deathban.PluginMain;
import com.winterhavenmc.deathban.messages.MessageId;
import com.winterhavenmc.deathban.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Help command implementation<br>
 * displays help and usage messages for plugin commands
 */
final class HelpSubcommand extends SubcommandAbstract implements Subcommand {

	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	HelpSubcommand(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.name = "help";
		this.usageString = "/deathban help [command]";
		this.description = MessageId.COMMAND_HELP_HELP;
		this.permission = "deathban.help";
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("help")) {
				for (String subcommand : subcommandRegistry.getKeys()) {
					if (sender.hasPermission(subcommandRegistry.getSubcommand(subcommand).getPermission())
							&& subcommand.startsWith(args[1].toLowerCase())
							&& !subcommand.equalsIgnoreCase("help")) {
						returnList.add(subcommand);
					}
				}
			}
		}

		return returnList;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission(permission)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_PERMISSION_HELP).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL_PERMISSION);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsageAll(sender);
			return true;
		}

		// get subcommand name
		String subcommandName = args.get(0);
		displayHelp(sender, subcommandName);
		return true;
	}


	/**
	 * Display help message and usage for a command
	 * @param sender the command sender
	 * @param commandName the name of the command for which to show help and usage
	 */
	void displayHelp(final CommandSender sender, final String commandName) {

		// get subcommand from map by name
		Subcommand subcommand = subcommandRegistry.getSubcommand(commandName);

		// if subcommand found in map, display help message and usage
		if (subcommand != null) {
			plugin.messageBuilder.compose(sender, subcommand.getDescription()).send();
			subcommand.displayUsage(sender);
		}

		// else display invalid command help message and usage for all commands
		else {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
			displayUsageAll(sender);
		}
	}


	/**
	 * Display usage message for all commands
	 * @param sender the command sender
	 */
	void displayUsageAll(final CommandSender sender) {

		plugin.messageBuilder.compose(sender, MessageId.COMMAND_HELP_USAGE_HEADER).send();
		for (String subcommandName : subcommandRegistry.getKeys()) {
			if (subcommandRegistry.getSubcommand(subcommandName) != null) {
				subcommandRegistry.getSubcommand(subcommandName).displayUsage(sender);
			}
		}
	}

}
