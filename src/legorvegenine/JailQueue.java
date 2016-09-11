package legorvegenine;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

public class JailQueue extends JavaPlugin implements Listener
{
	//Variables to store the file and its configuration
	FileConfiguration dataCFG;
	File data;
	
	@Override
	public void onEnable()
	{
		//Add XYZ variables for the location of the jail
		getConfig().addDefault("jailX", 0.0);
		getConfig().addDefault("jailY", 0.0);
		getConfig().addDefault("jailZ", 0.0);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		//If the data folder does not exist, we need to make it
		if (!getDataFolder().exists())
		{
			getDataFolder().mkdir();
		}
		
		//The file is in the plugin's data folder, named data.yml
		data = new File(getDataFolder(), "data.yml");
		
		//If the file does not exist, we need to create it
		if (!data.exists()) 
		{
			try 
			{
				//Create a new file to store data
				data.createNewFile();
            }
            catch (IOException e)
			{
            	//If the file is unable to be created, warn the console!
            	getLogger().severe("Could not create data.yml!");
            }
        }

		//Set the data file configuration to the file data
		dataCFG = YamlConfiguration.loadConfiguration(data);
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		saveConfig();
		saveData();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		//Console-Issued Commands
		if (sender == getServer().getConsoleSender())
		{
			//Jail Command
			if (cmd.getName().equalsIgnoreCase("jail"))
			{
				//No Specified Arguments -> Show Usage
				if(args.length == 0)
				{
					getLogger().info("Correct usage for /jail:");
					getLogger().info("/jail location <x> <y> <z>");
					getLogger().info("/jail add <playername> <reason>");
					getLogger().info("/jail release <playername>");
					getLogger().info("/jail list");
					return true;
				}
				
				//2 Arguments -> add <playername>
				if(args[0].equalsIgnoreCase("add") && args.length == 2)
				{
					addPlayerToData(args[1], "unspecified reason");
					getLogger().info(args[1] + " has been added to the Jail Queue without specified reason.");
				}
				//3+ Arguments -> add <playername> [reason]
				else if(args[0].equalsIgnoreCase("add") && args.length > 2)
				{
					String s = "";
					for(int i = 2; i < args.length; i++)
					{
						s += args[i];
						if(i < args.length - 1) s += " ";
					}
					addPlayerToData(args[1], s);
					getLogger().info(args[1] + " has been added to the Jail Queue with reason: \"" + s + "\".");
				}
				//2 Arguments -> remove <playername>
				else if(args[0].equalsIgnoreCase("remove") && args.length == 2)
				{
					if (removePlayerFromData(args[1]))
						getLogger().info(args[1] + " has been removed from the Jail Queue");
					else
						getLogger().info("Could not remove " + args[1] + " from the Jail Queue");
				}
				//1 Argument -> List the current players in queue
				else if(args[0].equalsIgnoreCase("list"))
				{
					getLogger().info("Current Players in the Jail Queue: \n" + dataCFG.saveToString());
				}
			}
			return true;
		}
		
		//With the console out of the way, we can assume a human is sending the command
		Player p = (Player)sender;
		
		//If the player is not an OP, do not proceed
		if (!p.hasPermission("operator"))
		{
			p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command");
			return true;
		}
		
		//Player-Issued Jail Commands
		if(cmd.getName().equalsIgnoreCase("jail") && sender instanceof Player)
		{
			//No Specified Arguments -> Show Usage
			if (args.length == 0)
			{
				//Skips all conditionals
			}
			//Location Set Command
			else if (args[0].equalsIgnoreCase("location"))
			{
				//Must specify XYZ
				if(args.length == 4)
				{
					try
					{
						//Try to convert the arguments to double values representative of coordinates
						double x = Double.valueOf(args[1]);
						double y = Double.valueOf(args[2]);
						double z = Double.valueOf(args[3]);

						//Sets the 3 variables in the config file
						getConfig().set("jailX", x);
						getConfig().set("jailY", y);
						getConfig().set("jailZ", z);
						saveConfig();
						p.sendMessage(ChatColor.GREEN + "Jail location set!");
					}
					catch(NumberFormatException e)
					{
						//Messages the player if they entered invalid arguments
						p.sendMessage(ChatColor.RED + "Invalid arguments. Use /jail location <x> <y> <z>");
					}
				}
				else
				{
					//Messages the player if they incorrectly entered the command arguments
					p.sendMessage(ChatColor.RED + "Incorrect arguments. Use /jail location <x> <y> <z>");
				}
				return true;
			}
			//Add Player Command
			else if (args[0].equalsIgnoreCase("add"))
			{
				//Add <playername>
				if (args.length == 2)
				{
					addPlayerToData(args[1], "unspecified reason");
					p.sendMessage(ChatColor.GREEN + args[1] + " has been added to the Jail Queue without specified reason.");
				}
				//Add <playername> [reason]
				else if (args.length > 2)
				{
					String s = "";
					for(int i = 2; i < args.length; i++)
					{
						s += args[i];
						if(i < args.length - 1) s += " ";
					}
					addPlayerToData(args[1], s);
					p.sendMessage(ChatColor.GREEN + args[1] + " has been added to the Jail Queue with reason: \"" + s + "\".");
				}
				return true;
			}
			//Remove Player Command
			else if(args[0].equalsIgnoreCase("remove"))
			{
				//Remove <playername> [extra garbage]
				if(args.length >= 2)
				{
					if (removePlayerFromData(args[1]))
						p.sendMessage(ChatColor.GREEN + args[1] + " has been removed from the Jail Queue");
					else
						p.sendMessage(ChatColor.RED + "Could not remove " + args[1] + " from the Jail Queue.");
				}
				return true;
			}
			//List Command
			else if (args[0].equalsIgnoreCase("list"))
			{
				p.sendMessage("Current Players in the Jail Queue: \n" + dataCFG.saveToString());
				return true;
			}
			p.sendMessage(ChatColor.UNDERLINE + "" + ChatColor.GREEN + "Correct usage for /jail:");
			p.sendMessage(ChatColor.GREEN + "/jail location <x> <y> <z>");
			p.sendMessage(ChatColor.GREEN + "/jail add <playername> <reason>");
			p.sendMessage(ChatColor.GREEN + "/jail remove <playername>");
			p.sendMessage(ChatColor.GREEN + "/jail list");
			return true;
		}
		
		return false;	
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		//If the player is not in the queue, cancel
		Player p = e.getPlayer();
		String name = p.getName().toLowerCase();
		
		if(dataCFG.isSet(name))
			jail(p);
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e)
	{
		//If the player is not in the queue, cancel
		Player p = e.getPlayer();
		String name = p.getName().toLowerCase();
		if(!dataCFG.isSet(name)) return;
		//If the jailed player is teleported because of the plugin, allow it
		if(e.getCause() == TeleportCause.PLUGIN) return;
		
		//Otherwise, cancel the teleport and yell at them
		e.setCancelled(true);
		p.sendMessage(ChatColor.DARK_RED + "You are jailed! You are not allowed to teleport.");
	}
	
	@SuppressWarnings("deprecation")
	public void addPlayerToData(String playerName, String reason)
	{
		//Add a player to the jail queue
		playerName = playerName.toLowerCase();
		dataCFG.set(playerName, reason);
		saveData();
		
		//And if they are online at the time, jail them immediately
		OfflinePlayer p = getServer().getOfflinePlayer(playerName);
		if(p.isOnline()) 
			jail(p);
	}
	
	public boolean removePlayerFromData(String playerName)
	{
		//Remove a player from the jail queue
		playerName = playerName.toLowerCase();
		//Return false if they were not found
		if (!dataCFG.isSet(playerName)) return false;
		dataCFG.set(playerName, null);
		saveData();
		//Return true if they were removed
		return true;
	}
	
	public void saveData() 
	{
		//Try to save the data
		try 
		{
			dataCFG.save(data);
		}
		catch (IOException e) 
		{
			//If the file is unable to be saved, warn the console!
			getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
		}
		
		//Reload the configuration as well
		dataCFG = YamlConfiguration.loadConfiguration(data);
	}
	
	public void jail(OfflinePlayer op)
	{
		//Take the offline player, cast them as a player, given they are online
		Player p = (Player)op;
		String name = p.getName().toLowerCase();
		//Yell at them.
		p.sendMessage(ChatColor.RED + "You have been jailed for: " + ChatColor.WHITE + "\"" + dataCFG.getString(name) + "\"");
		
		//Loads the variables from the config
		double x = getConfig().getDouble("jailX");
		double y = getConfig().getDouble("jailY");
		double z = getConfig().getDouble("jailZ");
		
		//Teleports the new p to spawn
		Location theJail = new Location(getServer().getWorld("world"), x, y, z);
		p.teleport(theJail);
	}
	
}