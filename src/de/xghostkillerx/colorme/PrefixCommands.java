package de.xghostkillerx.colorme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PrefixCommands implements CommandExecutor {
	
	ColorMe plugin;
	public PrefixCommands(ColorMe instance) {
		plugin = instance;
	}
	private String pluginPart = "prefix", message, target, prefix, senderName, world = "default", globalPrefix;
	private Double cost;

	// Commands for prefixing
	public boolean onCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
		// Reloads the configs
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("prefixer.reload")) {
				Actions.reload(sender);
				return true;
			}
			else {
				message = ColorMe.localization.getString("permission_denied");
				ColorMe.message(sender, null, message, null, null, null, null);
				return true;
			}
		}
		// Stop here if Prefixer is unwanted
		if (ColorMe.config.getBoolean("Prefixer") == false) {
			message = ColorMe.localization.getString("part_disabled");
			ColorMe.message(sender, null, message, null, null, null, null);
			return true;
		}
		// Displays the help
		if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
			Actions.help(sender, "prefix");
			return true;
		}
		// Sets the global prefix
		if (args.length > 1 && args[0].equalsIgnoreCase("global")) {
			globalPrefix = args[1];
			if (sender.hasPermission("prefixer.global")) {
				ColorMe.config.set("global_default.prefix", globalPrefix);
				plugin.saveConfig();
				message = ColorMe.localization.getString("global_change_prefix");
				ColorMe.message(sender, null, message, globalPrefix, null, null, null);
				return true;
			}
			else {
				message = ColorMe.localization.getString("permission_denied");
				ColorMe.message(sender, null, message, null, null, null, null);
				return true;
			}
		}
		// Removes a prefix
		if (args.length > 1 && args[0].equalsIgnoreCase("remove")) {
			world = "default";
			target = args[1].toLowerCase();
			// Support for "me" -> this is the senderName!
			if (target.equalsIgnoreCase("me")) {
				target = sender.getName().toLowerCase();
				// Tell console only ingame command
				if (sender instanceof ConsoleCommandSender) {
					message = ColorMe.localization.getString("only_ingame");
					ColorMe.message(sender, null, message, null, null, null, null);
					return true;
				}
			}
			senderName = sender.getName().toLowerCase();
			if (args.length > 2) {
				world = args[2].toLowerCase();
			}
			// Check for permission or self
			if (sender.hasPermission("prefixer.remove") || Actions.self(sender, target)) {
				// Trying to remove a prefix from a prefix-less player
				if (((!Actions.has(target, world, pluginPart) && ColorMe.players.contains(target + "." + pluginPart + "." + world)))
						|| !ColorMe.players.contains(target)) {
					// Self
					if (target.equalsIgnoreCase(senderName)) {
						message = ColorMe.localization.getString("no_prefix_self");
						ColorMe.message(sender, null, message, null, world, null, null);
						return true;
					}
					// Other
					message = ColorMe.localization.getString("no_prefix_other");
					ColorMe.message(sender, null, message, null, world, target, null);
					return true;
				}
				// Remove prefix
				Actions.remove(target, world, pluginPart);
				if (plugin.getServer().getPlayerExact(target) != null) {
					// Notify player is online
					Player player = plugin.getServer().getPlayerExact(target);
					message = ColorMe.localization.getString("removed_prefix_self");
					ColorMe.message(null, player, message, null, world, null, null);
				}
				// If player is offline just notify the sender
				if (!target.equalsIgnoreCase(senderName)) {
					message = ColorMe.localization.getString("removed_prefix_other");
					ColorMe.message(sender, null, message, null, world, target, null);
					return true;
				}
				return true;
			}
			// Deny access
			else {
				message = ColorMe.localization.getString("permission_denied");
				ColorMe.message(sender, null, message, null, null, null, null);
				return true;
			}
		}
		// Gets a prefix
		if (args.length > 1 && args[0].equalsIgnoreCase("get")) {
			world = "default";
			// If a player name is there, too
			target = args[1].toLowerCase();
			if (target.equalsIgnoreCase("me")) {
				target = sender.getName().toLowerCase();
				// Tell console only ingame command
				if (sender instanceof ConsoleCommandSender) {
					message = ColorMe.localization.getString("only_ingame");
					ColorMe.message(sender, null, message, null, null, null, null);
					return true;
				}
			}
			senderName = sender.getName().toLowerCase();
			if (args.length > 2) {
				world = args[2].toLowerCase();
			}
			Actions.get(target, world, pluginPart);
			// Check for permission or self
			if (sender.hasPermission("prefix.get") || Actions.self(sender, target)) {
				// Trying to get a prefix from a prefix-less player
				if (((!Actions.has(target, world, pluginPart) && ColorMe.players.contains(target)))
						|| !ColorMe.players.contains(target)) {
					// Self
					if (target.equalsIgnoreCase(senderName)) {
						message = ColorMe.localization.getString("no_prefix_self");
						ColorMe.message(sender, null, message, null, world, null, null);
						return true;
					}
					// Other
					message = ColorMe.localization.getString("no_prefix_other");
					ColorMe.message(sender, null, message, null, world, target, null);
					return true;
				}
				// Gets prefix
				if (target.equalsIgnoreCase(senderName)) {
					message = ColorMe.localization.getString("get_prefix_self");
					ColorMe.message(sender, null, message, prefix, world, null, null);
					return true;
				}
				message = ColorMe.localization.getString("get_prefix_other");
				ColorMe.message(sender, null, message, prefix, world, target, null);
				return true;
			}
			// Deny access
			else {
				message = ColorMe.localization.getString("permission_denied");
				ColorMe.message(sender, null, message, null, null, null, null);
				return true;
			}
		}
		// Prefixing
		if (args.length > 1) {
			world = "default";
			target = args[0].toLowerCase();
			if (target.equalsIgnoreCase("me")) {
				target = sender.getName().toLowerCase();
				// Tell console only ingame command
				if (sender instanceof ConsoleCommandSender) {
					message = ColorMe.localization.getString("only_ingame");
					ColorMe.message(sender, null, message, null, null, null, null);
					return true;
				}
			}
			prefix = args[1];
			senderName = sender.getName().toLowerCase();
			if (args.length > 2) {
				world = args[2].toLowerCase();
			}

			// If the prefixes are the same
			if (prefix.equalsIgnoreCase(Actions.get(target, world, pluginPart))) {
				if (senderName.equalsIgnoreCase(target)) {
					message = ColorMe.localization.getString("same_prefix_self");
					ColorMe.message(sender, null, message, null, world, null, null);
					return true;
				}	
				message = ColorMe.localization.getString("same_prefix_other");
				ColorMe.message(sender, null, message, null, world, target, null);
				return true;
			}

			// Self prefixing
			if (sender.hasPermission("prefix.self") && Actions.self(sender, target)) {
				// Without economy or costs are null
				cost = ColorMe.config.getDouble("costs.prefix");
				if (ColorMe.economy == null || cost == 0) {
					Actions.set(senderName, prefix, world, pluginPart);
					message = ColorMe.localization.getString("changed_prefix_self");
					ColorMe.message(sender, null, message, prefix, world, null, null);
					return true;
				}
				// With economy
				else if (ColorMe.economy != null){
					// Charge costs :)
					if (cost > 0 && ColorMe.economy.has(senderName, cost)) {
						ColorMe.economy.withdrawPlayer(senderName, cost);
						// Set prefix an notify sender
						Actions.set(senderName, prefix, world, pluginPart);
						message = ColorMe.localization.getString("charged");
						ColorMe.message(sender, null, message, null, null, null, cost);
						message = ColorMe.localization.getString("changed_prefix_self");
						ColorMe.message(sender, null, message, prefix, world, null, null);
						return true;
					}
					// If player hasn't got enough money
					else if (cost > 0 && ColorMe.economy.getBalance(senderName) < cost) {						
						message = ColorMe.localization.getString("not_enough_money_1");
						ColorMe.message(sender, null, message, null, null, null, null);
						message = ColorMe.localization.getString("not_enough_money_2");
						ColorMe.message(sender, null, message, null, null, null, cost);
						return true;
					}
				}
			}
			// Prefixing other
			else if (sender.hasPermission("prefix.other") && !Actions.self(sender, target)) {
				// Set the new prefix
				Actions.set(target, prefix, world, pluginPart);
				if (plugin.getServer().getPlayerExact(target) != null) {
					// Tell the affected player
					Player player = plugin.getServer().getPlayerExact(target);
					message = ColorMe.localization.getString("changed_prefix_self");
					ColorMe.message(null, player, message, prefix, world, null, null);
				}
				message = ColorMe.localization.getString("changed_prefix_other");
				ColorMe.message(sender, null, message, prefix, world, target, null);
				return true;
			}
			// Permission check failed
			else {
				message = ColorMe.localization.getString("permission_denied");
				ColorMe.message(sender, null, message, null, null, null, null);
				return true;
			}
		}
		return false;
	}
}