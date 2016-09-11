package legorvegenine;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


public class JailSentence implements Listener
{
	String n = "";
	Player p = null;
	String r = "";

	public JailSentence(String name){n = name;}
	public JailSentence(String name, String reason){n = name;r = reason;}

	public String toString()
	{
		return  n + " has been jailed for reason: " + r;
	}
	
	public void onJoin(PlayerJoinEvent e)
	{
		if(!e.getPlayer().getName().equalsIgnoreCase(n)) return;
		
		p = e.getPlayer();
		p.sendMessage(ChatColor.DARK_RED + "You have been jailed for \"" + r + "\".");
	}
	
	public void onTeleport (PlayerTeleportEvent e)
	{
		if(e.getPlayer() != p) return;
		e.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot teleport out of jail.");
		e.setCancelled(true);
	}
	
}
