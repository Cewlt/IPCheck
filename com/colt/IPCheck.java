package colt.ipcheck;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class IPCheck extends JavaPlugin implements Listener {
	private Map<String, String> altSet;
	private List<String> recentFinds;
	private List<String> staffNotify;
	
	@Override
	public void onLoad() {
		altSet = new HashMap<>();
		recentFinds = new ArrayList<>();
		staffNotify = new ArrayList<>();
	}
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		getCommand("ipc").setExecutor(new Commands(this));
		saveDefaultConfig();
		
		for(String set : getConfig().getStringList("sets")) {
			String[] splitSet = set.split(":");
			if(!altSet.containsKey(splitSet[0])) {
				altSet.put(splitSet[0], splitSet[1]);
			}
		}
	}
	
	@Override
	public void onDisable() {
		List<String> sets = new ArrayList<String>();
		for (Entry<String, String> entrySet : altSet.entrySet()) {
			String craftedDown = entrySet.getKey() + ":" + entrySet.getValue();
			sets.add(craftedDown);
		}
		getConfig().set("sets", sets);
		saveConfig();
		sets = null;
		altSet = null;
		recentFinds = null;
		staffNotify = null;
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
		Player player = event.getPlayer();
		if(player.hasPermission("ipcheck.bypass")) return;
		altSet.put(player.getName(), ip);
		if(altSet.get(player.getName()) == ip) {
			if(getAlts(player.getName(), ip) == "No Entries") return; 
			String alts = getAlts(player.getName(), ip);
			alertStaff("&4[ALERT] &a" + player.getName() + " &blogged in with the same IP as &c" +
					alts);
			DateFormat dateFormat = new SimpleDateFormat("h:mm a");
			String date = dateFormat.format(new Date());
			String message = "&a" + player.getName() + " &rlogged with the same IP as "
					+ alts;
			if(recentFinds.contains(message)) {
				recentFinds.remove(message);
			}
			message = "&c[" + date + "] " + message;
			if(recentFinds.size() >= 100) recentFinds = recentFinds.subList(50, recentFinds.size());
			recentFinds.add(ChatColor.translateAlternateColorCodes('&', message));
		}
		if(player.hasPermission("ipcheck.notify")) { 
			staffNotify.add(player.getName());
		}
	}
	
	@EventHandler
	public void playerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(staffNotify.contains(player.getName())) {
			staffNotify.remove(player.getName());
		}
	}

	public String getAlts(String sender, String IPkey) { 
		List<String> keys = new ArrayList<String>();
		boolean multiple = false;
		for(String key : altSet.keySet()) {
			if(key.equalsIgnoreCase(sender)) continue; 
			if(altSet.get(key).equalsIgnoreCase(IPkey)) {
				if(keys.size() >= 0) multiple = true;
				if(Bukkit.getPlayer(key) != null) {
					key = ChatColor.GREEN + key;
				} else {
					key = ChatColor.RED + key;
				}
				keys.add(key);
		    }
		}
		if(keys.isEmpty()) return "No Entries";
		return multiple ? StringUtils.join(keys, ChatColor.RESET + ", ") : keys.get(0);
	}

	private void alertStaff(String message) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		for(Player staff : Bukkit.getOnlinePlayers()) {
			if(staff.hasPermission("ipcheck.notify") && staffNotify.contains(staff.getName())) {
				staff.sendMessage(message);
			}
		}
	}
	
	public List<String> getRecentFinds() {
		return this.recentFinds;
	}
	
	public List<String> getStaffNotify() {
		return this.staffNotify;
	}
}
