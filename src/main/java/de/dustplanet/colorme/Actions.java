package de.dustplanet.colorme;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
// TagAPI
import org.kitteh.tag.TagAPI;
// PermissionsEx
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
// bPermissions
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
// GroupManager
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;

public class Actions {
    public ColorMe plugin;
    private Random rand = new Random();
    private static final String RAINBOW[] = { "DARK_RED", "RED", "GOLD", "YELLOW", "GREEN", "DARK_GREEN", "AQUA", "DARK_AQUA", "BLUE", "DARK_BLUE", "LIGHT_PURPLE", "DARK_PURPLE" };

    public Actions(ColorMe instance) {
	plugin = instance;
    }

    public static Actions hookIntoColorMe() {
	ColorMe plugin = (ColorMe) Bukkit.getPluginManager().getPlugin("ColorMe");
	if (plugin != null) {
	    return new Actions(plugin);
	}
	System.out.println("ColorMe instance not found returning null");
	return null;
    }

    /*
     * 
     * Group functions
     */

    /**
     * Gets a value of a group
     * 
     * @param groupName - the group name
     * @param world - the world
     * @param groupPart - the given plugin part
     * @return the string from the config
     */
    public String getGroup(String groupName, String world, String groupPart) {
	plugin.fileUtils.logDebug("Actions -> get");
	plugin.fileUtils.logDebug("Asked to get the " + groupPart + " from " + groupName + " in the world " + world);
	// Group in the config? Yes -> get the config, no -> nothing
	groupName = groupName.toLowerCase();
	world = world.toLowerCase();
	groupPart = groupPart.toLowerCase();
	return replaceThings(plugin.group.getString(groupName + "." + groupPart + "." + world));
    }


    /**
     * Checks if a group has got a value
     * 
     * @param groupName - the name of the group
     * @param world - the world
     * @param groupPart - the part (colors, prefix or suffix)
     * @return yes or no
     */
    public boolean hasGroup(String groupName, String world, String groupPart) {
	plugin.fileUtils.logDebug("Actions -> hasGroup");
	plugin.fileUtils.logDebug("Asked if " + groupName + " has got a " + groupPart + " in the world " + world);
	groupName = groupName.toLowerCase();
	world = world.toLowerCase();
	groupPart = groupPart.toLowerCase();
	if (plugin.group.contains(groupName + "." + groupPart + "." + world)) {
	    // if longer than 1 it's valid, return true - otherwise (means '')
	    // return false
	    return !plugin.group.getString(groupName + "." + groupPart + "." + world).isEmpty();
	}
	return false;
    }

    /**
     * Checks if a group exists
     * 
     * @param groupName - the group name
     * @return yes or no
     */
    public boolean existsGroup(String groupName) {
	plugin.fileUtils.logDebug("Actions -> existsGroup");
	plugin.fileUtils.logDebug("Asked if " + groupName + " exists");
	groupName = groupName.toLowerCase();
	return plugin.group.contains(groupName);
    }

    /**
     * Deletes a group
     * 
     * @param groupName - the group Name
     */
    public void deleteGroup(String groupName) {
	plugin.fileUtils.logDebug("Actions -> deleteGroup");
	plugin.fileUtils.logDebug("Asked to delete the group " + groupName);
	groupName = groupName.toLowerCase();
	List<String> members = plugin.group.getStringList(groupName + ".members");
	for (String member : members) {
	    plugin.players.set(member + ".group", "");
	}
	saveFile(plugin.playersFile, plugin.players);
	plugin.group.set(groupName, null);
	saveFile(plugin.groupsFile, plugin.group);
    }

    /**
     * Lists members of a group
     * 
     * @param groupName - the groupName
     * @return the list of members
     */
    public List<String> listMembers(String groupName) {
	plugin.fileUtils.logDebug("Actions -> listMembers");
	plugin.fileUtils.logDebug("Asked to list the members of " + groupName);
	groupName = groupName.toLowerCase();
	List<String> members = plugin.group.getStringList(groupName + ".members");
	return members;
    }

    /**
     * Sets a value of a group in a world
     * 
     * @param groupName - the name of the group
     * @param value - the value (color, prefix or suffix)
     * @param world - the world
     * @param groupPart - the part
     */
    public void setGroup(String groupName, String value, String world, String groupPart) {
	plugin.fileUtils.logDebug("Actions -> set");
	plugin.fileUtils.logDebug("Asked to set the " + groupPart + " of " + groupName + " in the world " + world);
	groupName = groupName.toLowerCase();
	world = world.toLowerCase();
	groupPart = groupPart.toLowerCase();
	// Write to the config and save and update the names
	plugin.group.set(groupName + "." + groupPart + "." + world, value);
	saveFile(plugin.groupsFile, plugin.group);
    }

    /**
     * Creates a group
     * 
     * @param groupName - the group name
     */
    public void createGroup(String groupName) {
	plugin.fileUtils.logDebug("Actions -> Asked to create the group " + groupName);
	groupName = groupName.toLowerCase();
	plugin.group.set(groupName + ".colors.default", "");
	plugin.group.set(groupName + ".prefix.default", "");
	plugin.group.set(groupName + ".suffix.default", "");
	plugin.group.set(groupName + ".members", "");
	saveFile(plugin.groupsFile, plugin.group);
    }

    /**
     * Adds a member to a group
     * 
     * @param groupName - the group name
     * @param name - the player name
     */
    public void addMember(String groupName, String name) {
	plugin.fileUtils.logDebug("Actions -> Add member " + name + " to the group " + groupName);
	name = name.toLowerCase();
	groupName = groupName.toLowerCase();
	plugin.players.set(name + ".group", groupName);
	saveFile(plugin.playersFile, plugin.players);
	List<String> members = plugin.group.getStringList(groupName + ".members");
	members.add(name);
	plugin.group.set(groupName + ".members", members);
	saveFile(plugin.groupsFile, plugin.group);
    }

    /**
     * Removes a member from a group
     * 
     * @param groupName - the name of the group
     * @param name - the name of the player
     */
    public void removeMember(String groupName, String name) {
	plugin.fileUtils.logDebug("Actions -> Remove member " + name + " from the group " + groupName);
	plugin.players.set(name + ".group", "");
	name = name.toLowerCase();
	groupName = groupName.toLowerCase();
	saveFile(plugin.playersFile, plugin.players);
	List<String> members = plugin.group.getStringList(groupName + ".members");
	members.remove(name);
	plugin.group.set(groupName + ".members", members);
	saveFile(plugin.groupsFile, plugin.group);
    }

    /**
     * Checks if the member is member of a given group
     * 
     * @param groupName - the name of the group
     * @param name - the player name
     * @return true or false, yes or no
     */
    public boolean isMember(String groupName, String name) {
	plugin.fileUtils.logDebug("Actions -> isMember");
	plugin.fileUtils.logDebug("Asked if " + name + " is a member of the group " + groupName);
	name = name.toLowerCase();
	groupName = groupName.toLowerCase();
	if (plugin.players.contains(name + ".group")) {
	    // If the string is the same -> return true
	    return groupName.equalsIgnoreCase(plugin.players.getString(name + ".group"));
	}
	return false;
    }

    /*
     * 
     * Global value functions
     */

    /**
     * Gets the global part
     * 
     * @param pluginPart - the part (color, suffix or prefix)
     * @return the string from the config
     */
    public String getGlobal(String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> getGlobal");
	plugin.fileUtils.logDebug("Asked to get the global " + pluginPart);
	pluginPart = pluginPart.toLowerCase();
	String string = plugin.config.getString("global_default." + pluginPart);
	String updatedString = replaceThings(string);
	return updatedString;
    }

    // Check if the global default is not null
    /**
     * Checks if the global values are used
     * 
     * @param pluginPart - the part (color, suffix, prefix)
     * @return yes or no
     */
    public boolean hasGlobal(String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> hasGlobal");
	plugin.fileUtils.logDebug("Asked if the global value of " + pluginPart + " is set");
	pluginPart = pluginPart.toLowerCase();
	return !plugin.config.getString("global_default." + pluginPart).isEmpty();
    }

    /**
     * Removes the global value
     * 
     * @param pluginPart - color/prefix or suffix
     */
    public void removeGlobal(String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> removeGlobal");
	plugin.fileUtils.logDebug("Asked to remove the global part of " + pluginPart);
	pluginPart = pluginPart.toLowerCase();
	plugin.config.set("global_default." + pluginPart, "");
	saveFile(plugin.configFile, plugin.config);
    }

    /*
     * 
     * Player functions
     */

    /**
     * Checks if the player has got a group or not
     * 
     * @param name - the player name
     * @return yes or not
     */
    public boolean playerHasGroup(String name) {
	plugin.fileUtils.logDebug("Actions -> playerHasGroup");
	plugin.fileUtils.logDebug("Asked if " + name + " has got a group");
	name = name.toLowerCase();
	if (plugin.players.contains(name + ".group")) {
	    // if longer than 1 it's valid, return true - otherwise (means '')
	    // return false
	    return !plugin.players.getString(name + ".group").isEmpty();
	}
	return false;
    }

    /**
     * Returns the group of a player
     * 
     * @param name - the player name
     * @return the name of the group
     */
    public String playerGetGroup(String name) {
	plugin.fileUtils.logDebug("Actions -> playerGetGroup");
	plugin.fileUtils.logDebug("Asked for the group of " + name);
	name = name.toLowerCase();
	return plugin.players.getString(name + ".group");
    }

    /**
     * Checks if the command sender is itself
     * 
     * @param sender - the sender of the command
     * @param name - the given name
     * @return yes or no!
     */
    public boolean self(CommandSender sender, String name) {
	plugin.fileUtils.logDebug("Actions -> self");
	return sender.equals(plugin.getServer().getPlayerExact(name));
    }

    /**
     * Returns the player color/suffix or prefix
     * 
     * @param name - the player name
     * @param world - the world
     * @param pluginPart - the part
     * @return the string out of the config! "" if not set!
     */
    public String get(String name, String world, String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> get");
	plugin.fileUtils.logDebug("Asked to get the " + pluginPart + " from " + name + " in the world " + world);
	// Player in the config? Yes -> get the config, no -> nothing
	name = name.toLowerCase();
	world = world.toLowerCase();
	pluginPart = pluginPart.toLowerCase();
	return replaceThings(plugin.players.getString(name + "." + pluginPart + "." + world));
    }

    /**
     * Sets the players color, suffix or prefix
     * 
     * @param name - the name
     * @param value - the actual "value"
     * @param world - the world
     * @param pluginPart - colors/prefix/suffix
     */
    public void set(String name, String value, String world, String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> set");
	plugin.fileUtils.logDebug("Asked to set the " + pluginPart + " from " + name + " in the world " + world);
	name = name.toLowerCase();
	world = world.toLowerCase();
	pluginPart = pluginPart.toLowerCase();
	// Write to the config and save and update the names
	plugin.players.set(name + "." + pluginPart + "." + world, value);
	saveFile(plugin.playersFile, plugin.players);
    }

    /**
     * Checks if the player has got a suffix/prefix or color
     * 
     * @param name - the name of the player
     * @param world - the world
     * @param pluginPart - the part, prefix/suffix/colors
     * @return true if the player has it or false if not
     */
    public boolean has(String name, String world, String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> has");
	plugin.fileUtils.logDebug("Asked if " + name + " has got (a) " + pluginPart + " in the world " + world);
	name = name.toLowerCase();
	world = world.toLowerCase();
	pluginPart = pluginPart.toLowerCase();
	if (plugin.players.contains(name + "." + pluginPart + "." + world)) {
	    // if longer than 1 it's valid, return true - otherwise (means '')
	    // return false
	    return !plugin.players.getString(name + "." + pluginPart + "." + world).isEmpty();
	}
	return false;
    }

    /**
     * Removes the color/prefix/suffix of a player
     * 
     * @param name - the player name
     * @param world - the world
     * @param pluginPart - the plugin part
     */
    public void remove(String name, String world, String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> remove");
	plugin.fileUtils.logDebug("Asked to remove the " + pluginPart + " from " + name + " in the world " + world);
	name = name.toLowerCase();
	world = world.toLowerCase();
	pluginPart = pluginPart.toLowerCase();
	// If the player has got a color
	if (has(name, world, pluginPart)) {
	    plugin.players.set(name + "." + pluginPart + "." + world, "");
	    saveFile(plugin.playersFile, plugin.players);
	    checkNames(name, world);
	    plugin.fileUtils.logDebug("Removed");
	}
    }

    /*
     * 
     * Replace function Colors and whole String
     */

    /**
     * Creates a random color
     * You may use this method in your own plugin,
     * please give me credits then!
     * 
     * @param name - name of the player
     * @return the updated name
     */
    public String randomColor(String name) {
	plugin.fileUtils.logDebug("Actions -> randomColor");
	plugin.fileUtils.logDebug("Asked to color the string " + name);
	StringBuffer buf = new StringBuffer();
	// As long as the length of the name isn't reached
	for (char ch : name.toCharArray()) {
	    // Roll the dice between the chat color values
	    int x = rand.nextInt(ChatColor.values().length);
	    // Color the character
	    buf.append(ChatColor.values()[x] + Character.toString(ch));
	}
	return buf.toString();
    }

    /**
     * Creates a rainbow effect
     * You may use this method in your own plugin,
     * please give me credits then!
     * 
     * @param name - name of the player
     * @return the updated name
     */
    public String rainbowColor(String name) {
	plugin.fileUtils.logDebug("Actions -> rainbowColor");
	plugin.fileUtils.logDebug("Asked to color the string " + name);
	// As long as the length of the name isn't reached
	int z = 0;
	StringBuffer buf = new StringBuffer();
	for (char ch : name.toCharArray()) {
	    // Reset if z reaches 12
	    if (z == 12) {
		z = 0;
	    }
	    // Add to the new name the colored character
	    buf.append(ChatColor.valueOf(RAINBOW[z]) + Character.toString(ch));
	    z++;
	}
	return buf.toString();
    }

    /**
     * Creates the custom color effect
     * 
     * @param color - the name of the color
     * @param text - the text to color with
     * @return the updated text
     */
    public String updateCustomColor(String color, String text) {
	plugin.fileUtils.logDebug("Actions -> updateCustomColor");
	plugin.fileUtils.logDebug("Asked to color the string " + text + " with the color " + color);
	// Get color
	String colorChars = ChatColor.translateAlternateColorCodes('\u0026', plugin.colors.getString(color));
	// No section sign or ampersand? Not valid; doesn't start with section
	// sign? Not valid! Ending without a char? Not valid!
	if (!colorChars.contains("\u00A7") || colorChars.contains("\u0026") || !colorChars.startsWith("\u00A7") || colorChars.endsWith("\u00A7")) {
	    return text;
	}
	// Split the color values
	String colorValues[] = colorChars.split(",");
	// For the text length
	StringBuffer buf = new StringBuffer();
	for (int i = 0, x = 0; i < text.length(); i++, x++) {
	    // No colors left? -> Start from 0
	    if (x == colorValues.length) {
		x = 0;
	    }
	    buf.append(colorValues[x] + text.charAt(i));
	}
	return buf.toString();
    }
    /**
     * Replaces all the colors
     * 
     * @param string - the message
     * @return the colored message
     */
    public String replaceThings(String string) {
	plugin.fileUtils.logDebug("Actions -> replaceThings");
	if (string == null) {
	    return "";
	}
	// While random is in there
	String sub;
	while (string.contains("\u0026random")) {
	    // Without random
	    int i = string.indexOf("\u0026random") + 7;
	    int z = string.length();
	    sub = string.substring(i, z);
	    // Stop if other ampersand or section sign is found
	    if (sub.contains("\u0026")) {
		sub = sub.substring(0, sub.indexOf("\u0026"));
	    }
	    if (sub.contains("\u00A7")) {
		sub = sub.substring(0, sub.indexOf("\u00A7"));
	    }
	    // Replace
	    string = string.replace(sub, randomColor(sub));
	    // Replace FIRST random
	    string = string.replaceFirst("\u0026random", "");
	    sub = "";
	}
	// While random (short) is in there
	while (string.contains("\u0026ran")) {
	    // Without random
	    int i = string.indexOf("\u0026ran") + 4;
	    int z = string.length();
	    sub = string.substring(i, z);
	    // Stop if other ampersand or section sign is found
	    if (sub.contains("\u0026")) {
		sub = sub.substring(0, sub.indexOf("\u0026"));
	    }
	    if (sub.contains("\u00A7")) {
		sub = sub.substring(0, sub.indexOf("\u00A7"));
	    }
	    // Replace
	    string = string.replace(sub, randomColor(sub));
	    // Replace FIRST random
	    string = string.replaceFirst("\u0026ran", "");
	    sub = "";
	}
	// While rainbow is in there
	while (string.contains("\u0026rainbow")) {
	    // Without rainbow
	    int i = string.indexOf("\u0026rainbow") + 8;
	    int z = string.length();
	    sub = string.substring(i, z);
	    // Stop if other ampersand or section sign is found
	    if (sub.contains("\u0026")) {
		sub = sub.substring(0, sub.indexOf("\u0026"));
	    }
	    if (sub.contains("\u00A7")) {
		sub = sub.substring(0, sub.indexOf("\u00A7"));
	    }
	    // Replace
	    string = string.replace(sub, rainbowColor(sub));
	    // Replace FIRST rainbow
	    string = string.replaceFirst("\u0026rainbow", "");
	    sub = "";
	}
	// While rainbow (short) is in there
	while (string.contains("\u0026rai")) {
	    // Without rainbow
	    int i = string.indexOf("\u0026rai") + 4;
	    int z = string.length();
	    sub = string.substring(i, z);
	    // Stop if other ampersand or section sign is found
	    if (sub.contains("\u0026")) {
		sub = sub.substring(0, sub.indexOf("\u0026"));
	    }
	    if (sub.contains("\u00A7")) {
		sub = sub.substring(0, sub.indexOf("\u00A7"));
	    }
	    // Replace
	    string = string.replace(sub, rainbowColor(sub));
	    // Replace FIRST rainbow
	    string = string.replaceFirst("\u0026rai", "");
	    sub = "";
	}
	// Normal color codes!
	string = ChatColor.translateAlternateColorCodes('\u0026', string);
	return string;
    }

    /*
     * 
     * Checking functions
     */

    /**
     * Checks the names of a player
     * 
     * @param name - the player name
     * @param world - the world
     */
    public void checkNames(String name, String world) {
	plugin.fileUtils.logDebug("Actions -> checkNames");
	plugin.fileUtils.logDebug("Asked to check the color of the player " + name + " in the world " + world);
	name = name.toLowerCase();
	world = world.toLowerCase();
	String color = null;
	// Check for color and valid ones, else restore
	// Check player specific world
	if (has(name, world, "colors")) {
	    color = get(name, world, "colors");
	}
	// Check player default
	else if (has(name, "default", "colors")) {
	    color = get(name, "default", "colors");
	}
	// If groups enabled
	else if (plugin.groups && plugin.ownSystem) {
	    // If group available
	    if (playerHasGroup(name)) {
		String group = playerGetGroup(name);
		// Group specific world
		if (hasGroup(group, world, "colors")) {
		    color = getGroup(group, world, "colors");
		}
		// Group default
		else if (hasGroup(group, "default", "colors")) {
		    color = getGroup(group, "default", "colors");
		}
	    }
	}
	// Group check for other plugins
	else if (plugin.groups && !plugin.ownSystem) {
	    color = getColorFromGroup(name, world);
	}
	// Then check if still nothing found and globalColor
	if (plugin.globalColor && color == null && hasGlobal("color")) {
	    color = getGlobal("color");
	}
	// Restore if nothing found...
	if (color == null) {
	    restoreName(name);
	    return;
	}
	// Check if valid
	String[] colors = color.split("-");
	for (String colorPart : colors) {
	    if (!validColor(colorPart)) {
		restoreName(name);
		return;
	    }
	}
	// Update the name
	updateName(name, color);
    }

    /**
     * Try to get the color from a specific group
     * from either bPermissions, PermisisonsEx or GroupManager
     * 
     * @param name - the name of the player
     * @param world - the world to check
     * @return - the color (if found) - may return NULL
     */
    public String getColorFromGroup(String name, String world) {
	String color = null;
	// Case PermissionsEx
	if (plugin.pex) {
	    PermissionUser user = PermissionsEx.getUser(name);
	    // Only first group
	    PermissionGroup group = user.getGroups(world)[0];
	    // Get the group color
	    color = group.getOption("color");
	}
	// Case bPermissions
	else if (plugin.bPermissions) {
	    // Only fist group
	    if (ApiLayer.getGroups(world, CalculableType.USER, name).length > 0) {
		String group = ApiLayer.getGroups(world, CalculableType.USER, name)[0];
		color = ApiLayer.getValue(world, CalculableType.GROUP, group, "color");
	    }
	}
	// Case GroupManager
	else if (plugin.groupManager) {
	    // World data -> then groups (only first) and finally the suffix and
	    // prefix!
	    OverloadedWorldHolder groupManager = plugin.groupManagerWorldsHolder.getWorldData(world);
	    String group = groupManager.getUser(name).getGroupName();
	    color = groupManager.getGroup(group).getVariables().getVarString("color");
	}
	return color;
    }

    /**
     * Updates the display name, tab list or the name above the head of a player
     * 
     * @param name - the name of the player
     * @param color - the color
     */
    public void updateName(String name, String color) {
	plugin.fileUtils.logDebug("Actions -> updateName");
	plugin.fileUtils.logDebug("Asked to update the color of " + name + " to the color " + color);
	// We always work with lowercase
	name = name.toLowerCase();
	Player player = plugin.getServer().getPlayerExact(name);
	// Make sure our player is valid! (online)
	if (player != null) {
	    String displayName = player.getDisplayName();
	    // Strip all colors out
	    String cleanDisplayName = ChatColor.stripColor(displayName);
	    // We will work on the name, based on the clean display name
	    String newDisplayName = cleanDisplayName;
	    // First restore temp to white
	    player.setDisplayName(cleanDisplayName);
	    // When we set if for the tabList we need to make sure the
	    // displayName is not too long (could be changed by other plugins!)
	    if (cleanDisplayName.length() > 16) {
		String tempDispName = cleanDisplayName.substring(0, 13) + "...";
		player.setPlayerListName(tempDispName);
	    } else {
		player.setPlayerListName(cleanDisplayName);
	    }
	    // Check for Async
	    if (plugin.playerTitle) {
		TagAPI.refreshPlayer(player);
	    }
	    // Support for multiple colors
	    String[] colors = color.split("-");
	    for (String colorPart : colors) {
		// Random
		if (colorPart.equalsIgnoreCase("random")) {
		    newDisplayName = randomColor(cleanDisplayName);
		}
		// Rainbow
		else if (colorPart.equalsIgnoreCase("rainbow")) {
		    newDisplayName = rainbowColor(cleanDisplayName);
		}
		// Custom colors
		else if (plugin.colors.contains(colorPart) && !plugin.colors.getString(colorPart).isEmpty()) {
		    newDisplayName = updateCustomColor(colorPart, cleanDisplayName);
		} else {
		    newDisplayName = ChatColor.valueOf(colorPart.toUpperCase()) + newDisplayName;
		}
	    }
	    // Name color
	    if (plugin.displayName) {
		player.setDisplayName(newDisplayName);
	    }
	    // Check for playerList
	    if (plugin.tabList) {
		// Shorten it, if too long
		if (newDisplayName != null && !newDisplayName.equals("")) {
		    if (newDisplayName.length() > 16) {
			newDisplayName = newDisplayName.substring(0, 13) + "...";
		    }
		    player.setPlayerListName(newDisplayName);
		}
	    }
	    // Check if TagAPI should be used -> above the head!
	    if (plugin.playerTitle && player.hasPermission("plugin.nametag")) {
		if (!color.equalsIgnoreCase("rainbow") && !color.equalsIgnoreCase("random")) {
		    TagAPI.refreshPlayer(player);
		}
	    }
	}
    }

    /**
     * Restores the name of a player to a clean white one
     * 
     * @param name - the name of the player
     */
    public void restoreName(String name) {
	plugin.fileUtils.logDebug("Actions -> restoreName");
	plugin.fileUtils.logDebug("Asked to restore the name " + name);
	// We always work with lowercase
	name = name.toLowerCase();
	// Make sure player is online/valid
	Player player = plugin.getServer().getPlayerExact(name);
	if (player != null) {
	    plugin.fileUtils.logDebug("Player found and valid");
	    String displayName = player.getDisplayName();
	    // Strip color out
	    String cleanDisplayName = ChatColor.stripColor(displayName);
	    player.setDisplayName(cleanDisplayName);
	    if (plugin.tabList) {
		// If the TAB name is longer than 16 shorten it!
		String newName = cleanDisplayName;
		if (newName.length() > 16) {
		    newName = cleanDisplayName.substring(0, 12) + ChatColor.WHITE + "..";
		}
		player.setPlayerListName(newName);
	    }
	    // Check if TagAPI should be used -> above the head!
	    if (plugin.playerTitle && player.hasPermission("plugin.nametag")) {
		TagAPI.refreshPlayer(player);
	    }
	}
    }

    /**
     * Checks if the config is recognized
     * Possible are custom colors, random or
     * rainbow and standard ones
     * 
     * @param color - name of the color
     * @return the result - yes or no
     */
    public boolean validColor(String color) {
	plugin.fileUtils.logDebug("Actions -> validColor");
	// If it's random or rainbow -> possible
	if (color.equalsIgnoreCase("rainbow") || color.equalsIgnoreCase("random")) {
	    plugin.fileUtils.logDebug("Color " + color + " is valid");
	    return true;
	}
	// Custom color? (Must contain something!!! NOT '' or null)
	if (plugin.colors.contains(color) && !plugin.colors.getString(color).isEmpty()) {
	    plugin.fileUtils.logDebug("Color " + color + " is valid");
	    return true;
	}
	// Third place, cause random and rainbow aren't possible normally and
	// custom colors not, too ;)
	else {
	    for (ChatColor value : ChatColor.values()) {
		// Check if the color is one of the 17
		if (color.equalsIgnoreCase(value.name().toLowerCase())) {
		    plugin.fileUtils.logDebug("Color " + color + " is valid");
		    return true;
		}
	    }
	    plugin.fileUtils.logDebug("Color " + color + " is invalid");
	    return false;
	}
    }

    /**
     * Checks if the given color is disabled in the config
     * 
     * @param color - name of the color
     * @return yes or no - disabled or not
     */
    public boolean isDisabled(String color) {
	plugin.fileUtils.logDebug("Actions -> isDisabled");
	if (plugin.config.getBoolean("colors." + color.toLowerCase())) {
	    plugin.fileUtils.logDebug("Color " + color + " is enabled");
	    return false;
	}
	// Custom color? (Must contain something!!! NOT '' or null)
	else if (plugin.colors.contains(color) && !plugin.colors.getString(color).isEmpty() && plugin.config.getBoolean("colors.custom")) {
	    plugin.fileUtils.logDebug("Color " + color + " is enabled");
	    return false;
	}
	plugin.fileUtils.logDebug("Color " + color + " is disabled");
	return true;
    }

    /**
     * Checks if the given color is a standard color
     * 
     * @param color - name of the color
     * @return yes or no - whether the color is standard or not
     */
    public boolean isStandard(String color) {
	plugin.fileUtils.logDebug("Actions -> isStandard");
	color = color.toUpperCase();
	for (ChatColor chatColor : ChatColor.values()) {
	    if (chatColor.name().equalsIgnoreCase(color)) {
		return true;
	    }
	}
	return false;
    }

    /*
     * 
     * Misc funtions
     */

    // Displays the specific help
    public void help(CommandSender sender, String pluginPart) {
	plugin.fileUtils.logDebug("Actions -> help");
	// Help variable, currently we have 9 sites
	int z = 9;
	// Groups? Then 12 sites
	if (pluginPart.equals("group")) {
	    z = 12;
	}
	for (int i = 1; i <= z; i++) {
	    String message = plugin.localization.getString("help_" + pluginPart + "_" + Integer.toString(i));
	    plugin.message(sender, null, message, null, null, null, null);
	}
    }

    // Reloads the plugin
    public void reload(CommandSender sender) {
	plugin.fileUtils.logDebug("Actions -> reload");
	plugin.loadConfigsAgain();
	String message = plugin.localization.getString("reload");
	plugin.message(sender, null, message, null, null, null, null);
    }

    // The list of colors
    public void listColors(CommandSender sender) {
	plugin.fileUtils.logDebug("Actions -> listColors");
	String message = plugin.localization.getString("color_list");
	plugin.message(sender, null, message, null, null, null, null);
	StringBuffer buf = new StringBuffer();
	// As long as all colors aren't reached, including magic manual
	for (ChatColor value : ChatColor.values()) {
	    // get the name from the integer
	    String color = value.name().toLowerCase();
	    String colorChar = Character.toString(value.getChar());
	    if (colorChar.equalsIgnoreCase("r")
		    || colorChar.equalsIgnoreCase("n")
		    || colorChar.equalsIgnoreCase("m") 
		    || colorChar.equalsIgnoreCase("k")) {
		continue;
	    }
	    // color the name of the color
	    if (plugin.config.getBoolean("colors." + color)) {
		buf.append(ChatColor.valueOf(color.toUpperCase()) + color + " (\u0026" + colorChar + ") " + ChatColor.WHITE);
	    }
	}
	if (plugin.config.getBoolean("colors.strikethrough")) {
	    buf.append(ChatColor.STRIKETHROUGH + "striketrough" + ChatColor.WHITE + " (\u0026m) ");
	}
	if (plugin.config.getBoolean("colors.underline")) {
	    buf.append(ChatColor.UNDERLINE + "underline" + ChatColor.WHITE + " (\u0026n) ");
	}
	if (plugin.config.getBoolean("colors.magic")) {
	    buf.append("magic (" + ChatColor.MAGIC + "a" + ChatColor.WHITE + ", \u0026k) ");
	}
	// Include custom colors
	if (plugin.config.getBoolean("colors.random")) {
	    buf.append(randomColor("random (\u0026random)" + " "));
	}
	if (plugin.config.getBoolean("colors.rainbow")) {
	    buf.append(rainbowColor("rainbow (\u0026rainbow)") + " ");
	}
	if (plugin.config.getBoolean("colors.custom")) {
	    buf.append(ChatColor.RESET);
	    for (String color : plugin.colors.getKeys(false)) {
		buf.append(color + " ");
	    }
	}
	if (plugin.config.getBoolean("colors.mixed")) {
	    buf.append(ChatColor.RESET + "" + ChatColor.DARK_RED + "\nMixed colors are enabled. Example: blue-bold");
	}
	sender.sendMessage(buf.toString());
    }

    // Saves a file
    private void saveFile(File file, FileConfiguration config) {
	try {
	    config.save(file);
	    plugin.fileUtils.logDebug("Saved to the " + file);
	} catch (IOException e) {
	    plugin.getServer().getLogger().warning("Failed to save the " + file + "! Please report this! IOException");
	    plugin.fileUtils.logDebug("Failed to save");
	    plugin.fileUtils.logDebugException(e);
	}
    }

    /**
     * Checks if a message contains a blacklisted word
     * 
     * @param message you want to check
     * @return null if nothing was found, otherwise the found word will be returned
     */
    public String containsBlackListedWord(String message) {
	message = replaceThings(message);
	message = ChatColor.stripColor(message).toLowerCase();
	// Only a-z (lower and uppercase and 0-9 are valid
	message = message.replaceAll("[^a-zA-Z0-9]+", "");
	for (String s : plugin.bannedWords) {
	    if (message.contains(s.toLowerCase())) {
		return s;
	    }
	}
	// Nothing found
	return null;
    }
}