package de.dustplanet.colorme.listeners;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import de.dustplanet.colorme.Actions;
import de.dustplanet.colorme.ColorMe;

/**
 * ColorMe for CraftBukkit/Bukkit
 * Handles the player name (TagAPI)
 * 
 * Refer to the forum thread:
 * http://bit.ly/colormebukkit
 * Refer to the dev.bukkit.org page:
 * http://bit.ly/bukkitdevcolorme
 *
 * @author xGhOsTkiLLeRx
 * @thanks to Valrix for the original ColorMe plugin!!
 * 
 */

public class ColorMeTagAPIListener implements Listener {
	private ColorMe plugin;
	private Actions actions;
	public ColorMeTagAPIListener(ColorMe instance, Actions actionsInstance) {
		plugin = instance;
		actions = actionsInstance;
	}

	@EventHandler(ignoreCancelled = true)
	public void onNameTag(final PlayerReceiveNameTagEvent event) {
		if (event.getNamedPlayer().hasPermission("colorme.nametag")) {
			if (plugin.removeNameAboveHead) {
				event.setTag("");
				return;
			}
			String name = event.getNamedPlayer().getName();
			String world = event.getNamedPlayer().getWorld().getName();
			// Fallback
			String color = "WHITE";
			if (actions.has(name, world, "colors"))	color = actions.get(name, world, "colors");
			// Check player default
			else if (actions.has(name, "default", "colors")) color = actions.get(name, "default", "colors");
			// If groups enabled
			else if (plugin.groups) {
				if (plugin.ownSystem) {
					// If group available
					if (actions.playerHasGroup(name)) {
						String group = actions.playerGetGroup(name);
						// Group specific world
						if (actions.hasGroup(group, world, "colors")) color = actions.getGroup(group, world, "colors");
						// Group default
						else if (actions.hasGroup(group, "default", "colors")) color = actions.getGroup(group, "default", "colors");
					}
				}
				else if (plugin.pex) {
					PermissionUser user = PermissionsEx.getUser(name);
					// Only first group
					PermissionGroup group = user.getGroups(world)[0];
					// Get the group color
					color = group.getOption("color");
				}
				else if (plugin.bPermissions) {
					// Only fist group
					if (ApiLayer.getGroups(world, CalculableType.USER, name).length > 0) {
						String group = ApiLayer.getGroups(world, CalculableType.USER, name)[0];
						color = ApiLayer.getValue(world, CalculableType.GROUP, group, "color");
					}
				}
				else if (plugin.groupManager) {
					// World data -> then groups (only first) & finally the suffix & prefix!
					try {
						Plugin groupManagerPlugin = plugin.getServer().getPluginManager().getPlugin("GroupManager");
						WorldsHolder groupManagerWorldsHolder = ((GroupManager) groupManagerPlugin).getWorldsHolder();
						OverloadedWorldHolder groupManager = groupManagerWorldsHolder.getWorldData(world);
						if (groupManager != null) {
							String group = groupManager.getUser(name).getGroupName();
							color = groupManager.getGroup(group).getVariables().getVarString("color");
						}
					}
					catch (NullPointerException e) {}
				}
			}
			// Then check if still nothing found and globalColor
			if (plugin.globalColor && color == null) {
				if (actions.hasGlobal("color")) color = actions.getGlobal("color");
			}
			// Check if valid
			String[] colors = color.split("-");
			for (String colorActual : colors) {
				// Special case: not a standard, use first char then!
				if (!actions.isStandard(colorActual)) {
					if (color.equals("rainbow")) {
						colorActual = "dark_red";
					}
					// Generate a random color
					else if (color.equals("random")) {
						int x = (int) (Math.random() * ChatColor.values().length);
						colorActual = ChatColor.values()[x].name().toLowerCase();
					}
					else {
						String colorChars = ChatColor.translateAlternateColorCodes('\u0026', plugin.colors.getString(color));
						// No � or &? Not valid; doesn't start with �? Not valid! Ending without a char? Not valid!
						if (!colorChars.contains("\u00A7") || colorChars.contains("\u0026") || !colorChars.startsWith("\u00A7") || colorChars.endsWith("\u00A7")) colorActual = "white";
						// Split the color values
						else {
							String colorValues[] = colorChars.split(",");
							// Special here, since we aleady have got the � sign!
							colorActual = colorValues[0];
							name = colorActual + name;
							continue;
						}
					}
				}
				name = ChatColor.valueOf(colorActual.toUpperCase()) + name;
			}
			if (name != "" && name != null) event.setTag(name);
		}
	}
}