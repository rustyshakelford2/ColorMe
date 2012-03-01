package de.xghostkillerx.colorme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
// Economy (Vault)
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

/**
 * ColorMe for CraftBukkit/Bukkit
 * Handles some general stuff.
 * And the checks for color etc.
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

public class ColorMe extends JavaPlugin {
	public final static Logger log = Logger.getLogger("Minecraft");
	private final ColorMePlayerListener playerListener = new ColorMePlayerListener(this);
	public Economy economy = null;
	public static FileConfiguration config;
	public static FileConfiguration players;
	public static FileConfiguration localization;
	public static File configFile;
	public static File playersFile;
	public static File localizationFile;
	public static boolean spoutEnabled;
	private ColorMeCommands colorExecutor;
	private PrefixCommands prefixExecutor;
	private SuffixCommands suffixExecutor;
	public List<String> values = new ArrayList<String>();

	// Shutdown
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion()	+ " has been disabled!");
	}

	// Start
	public void onEnable() {
		// Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);

		// Player colors config		
		playersFile = new File(getDataFolder(), "players.yml");
		// Copy if the config doesn't exist
		if (!playersFile.exists()) {
			playersFile.getParentFile().mkdirs();
			copy(getResource("players.yml"), playersFile);
		}
		// Try to load
		try {
			players = YamlConfiguration.loadConfiguration(playersFile);
		}
		// Log if failed
		catch (Exception e) {
			log.warning("ColorMe failed to load the players.yml! Please report this!");
		}

		// Config
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
		config = this.getConfig();
		loadConfig();

		// Localization
		localizationFile = new File(getDataFolder(), "localization.yml");
		if(!localizationFile.exists()){
			localizationFile.getParentFile().mkdirs();
			copy(getResource("localization.yml"), localizationFile);
		}
		// Try to load
		try {
			localization = YamlConfiguration.loadConfiguration(localizationFile);
			loadLocalization();
		}
		// If it failed, tell it
		catch (Exception e) {
			log.warning("ColorMe failed to load the localization!");
		}

		// Refer to ColorMeCommands
		colorExecutor = new ColorMeCommands(this);
		getCommand("color").setExecutor(colorExecutor);

		// Refer to PrefixCommands
		prefixExecutor = new PrefixCommands(this);
		getCommand("prefix").setExecutor(prefixExecutor);

		// Refer to SuffixCommands
		suffixExecutor = new SuffixCommands(this);
		getCommand("suffix").setExecutor(suffixExecutor);

		// Message
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");

		// Check for Vault
		Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
		if (vault != null & vault instanceof Vault) {
			// If Vault is enabled, load the economy
			log.info(pdfFile.getName() + " loaded Vault successfully");
			setupEconomy();
		} else {
			// Else tell the admin about the missing of Vault
			log.warning("Vault was NOT found! Running without economy!");
		}

		//Check for Spout
		Plugin spout = this.getServer().getPluginManager().getPlugin("Spout");
		if (spout != null) {
			log.info(String.format(pdfFile.getName() + " loaded Spout successfully"));
			// Spout is enabled
			spoutEnabled = true;
		}
		else {
			log.warning("Running without Spout!");
			// Spout is disabled
			spoutEnabled = false;
		}

		// Stats

		checkStatsStuff();
		try {
			Metrics metrics = new Metrics();
			// Custom plotter for each part
			for (int i = 0; i < values.size(); i++) {
				final String value = values.get(i);
				metrics.addCustomData(this, new Metrics.Plotter() {
					@Override
					public String getColumnName() {
						return value;
					}

					@Override
					public int getValue() {
						return 1;
					}
				});
			}
			metrics.beginMeasuringPlugin(this);
		}
		catch (IOException e) {}
	}

	private void checkStatsStuff() {
		if (config.getBoolean("Prefixer") == true) {
			values.add("Prefixer");
		}
		if (config.getBoolean("Suffixer") == true) {
			values.add("Suffixer");
		}
		if (config.getBoolean("ColorMe.displayName") == true) {
			values.add("ColorMe - displayName");
		}
		if (config.getBoolean("ColorMe.tabList") == true) {
			values.add("ColorMe - tabList");
		}
		if (config.getBoolean("ColorMe.playerTitle") == true) {
			values.add("ColorMe - playerTitle");
		}
	}

	// Loads the config at the start
	public void loadConfig() {
		config.options().header("For help please refer to http://bit.ly/colormebukkit or http://bit.ly/bukkitdevcolorme");
		config.addDefault("costs.color", 5.00);
		config.addDefault("costs.prefix", 5.00);
		config.addDefault("costs.suffix", 5.00);
		config.addDefault("global_default.prefix", "");
		config.addDefault("global_default.suffix", "");
		config.addDefault("global_default.color", "");
		config.addDefault("Prefixer", true);
		config.addDefault("Suffixer", true);
		config.addDefault("ColorMe.displayName", true);
		config.addDefault("ColorMe.tabList", true);
		config.addDefault("ColorMe.playerTitle", true);
		for (ChatColor value : ChatColor.values()) {
			// get the name from the integer
			String color = value.name().toLowerCase();
			// write to the config
			config.addDefault("colors." + color, true);
		}
		config.addDefault("colors.random", true);
		config.addDefault("colors.rainbow", true);
		config.options().copyDefaults(true);
		saveConfig();
	}

	// Loads the localization
	public void loadLocalization() {
		localization.options().header("The underscores are used for the different lines!");
		localization.addDefault("permission_denied", "&4You don't have the permission to do this!");
		localization.addDefault("part_disabled", "&4Sorry, but this command and plugin part is disabled!");
		localization.addDefault("only_ingame", "&4Sorry, this command can only be run from ingame!");
		localization.addDefault("color_list", "Color list: (the & values are used for prefix/suffix!)");
		localization.addDefault("reload", "&2ColorMe version &4%version &2reloaded!");
		localization.addDefault("charged", "&2You have been charged &4$%costs");
		localization.addDefault("not_enough_money_1", "&4Sorry, you don't have enough money to do this.");
		localization.addDefault("not_enough_money_2", "&4It costs &e%costs &4to do this!");
		localization.addDefault("no_color_self", "&eYou &4don't have a colored name in the world &e%world");
		localization.addDefault("no_color_other", "&e%player &4doesn't have a colored name in the world &e%world");
		localization.addDefault("no_color_global", "&4The global color isn't set!");
		localization.addDefault("same_color_self", "&eYou &4already have got this color in the world &e%world");
		localization.addDefault("same_color_other", "&e%player &4already has got this color in the world &e%world");
		localization.addDefault("same_color_global", "&4The global color is already this color!");
		localization.addDefault("invalid_color", "&4'&e%color&4' is not a supported color.");
		localization.addDefault("disabled_color", "&4Sorry, but the color '&e%color&4' is disabled.");
		localization.addDefault("removed_color_self", "&eYour &2name color in the world &e%world &2has been removed.");
		localization.addDefault("removed_color_other", "&2Removed &e%player&2's color in the world &e%world.");
		localization.addDefault("removed_color_global", "&2Removed the global color.");
		localization.addDefault("changed_color_self", "&eYour &2name color has been changed to &e%color &2in the world &e%world");
		localization.addDefault("changed_color_other", "&2Changed &e%player&2's color to &e%color &2in the world &e%world");
		localization.addDefault("changed_color_global", "&2The global color has been changed to &e%color");
		localization.addDefault("get_color_self", "&eYou &2have got the color %color &2in the world &e%world");
		localization.addDefault("get_color_other", "&e%player &2has got the color %color &2in the world &e%world");
		localization.addDefault("get_color_global", "&2The global color is &e%color");
		localization.addDefault("help_color_1", "&2Welcome to the ColorMe version &4%version &2help!");
		localization.addDefault("help_color_2", "&4 <> = Required, [] = Optional");
		localization.addDefault("help_color_3", "/<command> help - Shows the help");
		localization.addDefault("help_color_4", "/<command> list - Shows list of colors");
		localization.addDefault("help_color_5", "/<command> get <name> [world] - Gets the actual color");
		localization.addDefault("help_color_6", "/<command> remove <name> [world] - Removes color");
		localization.addDefault("help_color_7", "/<command> me <color> [world] - Sets your own color");
		localization.addDefault("help_color_8", "/<command> <name> <color> [world] - Sets player's color");
		localization.addDefault("help_color_9", "/<command> global <color> - Sets the global color");
		localization.addDefault("no_prefix_self", "&eYou &4don't have a prefix in the world &e%world");
		localization.addDefault("no_prefix_other", "&e%player &4doesn't have a prefix in the world &e%world");
		localization.addDefault("no_prefix_global", "&4The global prefix isn't set!");
		localization.addDefault("same_prefix_self", "&eYou &4already have got this prefix in the world &e%world");
		localization.addDefault("same_prefix_other", "&e%player &4already has got this color in the world &e%world");
		localization.addDefault("same_prefix_global", "&4The global prefix is already this prefix!");
		localization.addDefault("removed_prefix_self", "&eYour &2prefix in the world &e%world &2has been removed.");
		localization.addDefault("removed_prefix_other", "&2Removed &e%player&2's prefix in the world &e%world.");
		localization.addDefault("removed_prefix_global", "&2Removed the global prefix.");
		localization.addDefault("changed_prefix_self", "&eYour &2prefix has been changed to &e%prefix &2in the world &e%world");
		localization.addDefault("changed_prefix_other", "&2Changed &e%player&2's prefix to &e%prefix &2in the world &e%world");
		localization.addDefault("changed_prefix_global", "&2The global prefix has been changed to &e%prefix");
		localization.addDefault("get_prefix_self", "&eYou &2have got the prefix %prefix &2in the world &e%world");
		localization.addDefault("get_prefix_other", "&e%player &2has got the prefix %prefix %2in the world &e%world");
		localization.addDefault("get_prefix_global", "&2The global prefix is &e%prefix");
		localization.addDefault("help_prefix_1", "&2Welcome to the Prefixer (part of ColorMe) version &4%version &2help!");
		localization.addDefault("help_prefix_2", "&4 <> = Required, [] = Optional");
		localization.addDefault("help_prefix_3", "/<command> help - Shows the help");
		localization.addDefault("help_prefix_4", "/color list - Shows list of colors");
		localization.addDefault("help_prefix_5", "/<command> get <name> [world] - Gets the actual prefix");
		localization.addDefault("help_prefix_6", "/<command> remove <name> [world] - Removes prefix");
		localization.addDefault("help_prefix_7", "/<command> me <prefix> [world] - Sets your own prefix");
		localization.addDefault("help_prefix_8", "/<command> <name> <prefix> [world] - Sets player's prefix");
		localization.addDefault("help_prefix_9", "/<command> global <prefix> - Sets the global prefix");
		localization.addDefault("no_suffix_self", "&eYou &4don't have a suffix in the world &e%world");
		localization.addDefault("no_suffix_other", "&e%player &4doesn't have a suffix in the world &e%world");
		localization.addDefault("no_suffix_global", "&4The global suffix isn't set!");
		localization.addDefault("same_suffix_self", "&eYou &4already have got this suffix in the world &e%world");
		localization.addDefault("same_suffix_other", "&e%player &4already has got this color in the world &e%world");
		localization.addDefault("same_suffix_global", "&4The global suffix is already this suffix!");
		localization.addDefault("removed_suffix_self", "&eYour &2suffix in the world &e%world &2has been removed.");
		localization.addDefault("removed_suffix_other", "&2Removed &e%player&2's suffix in the world &e%world.");
		localization.addDefault("removed_suffix_global", "&2Removed the global suffix.");
		localization.addDefault("changed_suffix_self", "&eYour &2suffix has been changed to &e%suffix &2in the world &e%world");
		localization.addDefault("changed_suffix_other", "&2Changed &e%player&2's suffix to &e%suffix &2in the world &e%world");
		localization.addDefault("changed_suffix_global", "&2The global suffix has been changed to &e%suffix");
		localization.addDefault("get_suffix_self", "&eYou &2have got the suffix %suffix &2in the world &e%world");
		localization.addDefault("get_suffix_other", "&e%player &2has got the suffix %suffix %2in the world &e%world");
		localization.addDefault("get_suffix_global", "&2The global suffix is &e%suffix");
		localization.addDefault("help_suffix_1", "&2Welcome to the Suffixer (part of ColorMe) version &4%version &2help!");
		localization.addDefault("help_suffix_2", "&4 <> = Required, [] = Optional");
		localization.addDefault("help_suffix_3", "/<command> help - Shows the help");
		localization.addDefault("help_suffix_4", "/color list - Shows list of colors");
		localization.addDefault("help_suffix_5", "/<command> get <name> [world] - Gets the actual suffix");
		localization.addDefault("help_suffix_6", "/<command> remove <name> [world] - Removes suffix");
		localization.addDefault("help_suffix_7", "/<command> me <suffix> [world] - Sets your own suffix");
		localization.addDefault("help_suffix_8", "/<command> <name> <suffix> [world] - Sets player's suffix");
		localization.addDefault("help_prefix_9", "/<command> global <suffix> - Sets the global suffix");
		localization.options().copyDefaults(true);
		saveLocalization();
	}

	// Reloads the config via command /colorme reload
	public static void loadConfigsAgain() {
		try {
			config.load(configFile);
			saveConfigs();
			players.load(playersFile);
			savePlayers();
			localization.load(localizationFile);
			saveLocalization();
		}
		catch (Exception e) {
			log.warning("ColorMe failed to load the configs! Please report this!");
		}
	}

	// Try to save the players.yml
	public static void savePlayers() {
		try {
			players.save(playersFile);
		} catch (Exception e) {
			log.warning("ColorMe failed to save the players.yml! Please report this!");
		}
	}

	// Saves the localization
	public static void saveLocalization() {
		try {
			localization.save(localizationFile);
		}
		catch (IOException e) {
			log.warning("ColorMe failed to save the localization! Please report this!");
		}
	}

	// Saves the config
	public static void saveConfigs() {
		try {
			config.save(configFile);
		}
		catch (IOException e) {
			log.warning("ColorMe failed to save the config! Please report this!");
		}
	}

	// If no config is found, copy the default one(s)!
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Initialized to work with Vault
	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	// Message sender
	public static void message(CommandSender sender, Player player, String message, String value, String world, String target, Double cost) {
		if (message != null) {
			message = message
					.replaceAll("&([0-9a-fk])", "\u00A7$1")
					.replaceAll("%world", world)
					.replaceAll("%color", value)
					.replaceAll("%prefix", value)
					.replaceAll("%suffix", value)
					.replaceAll("%player", target)
					.replaceAll("%version", "3.4");
			if (cost != null) {
				//@SuppressWarnings("static-access")
				message = message.replaceAll("%costs", Double.toString(cost));
			}
			if (player != null) {
				player.sendMessage(message);
			}
			else if (sender != null) {
				sender.sendMessage(message);
			}
		}
		// If message is null
		else {
			if (player != null) {
				player.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
			}
			else if (sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "Somehow this message is not defined. Please check your localization.yml");
			}
		}
	}
}