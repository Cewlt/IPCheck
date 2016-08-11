package colt.ipcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.net.InetAddresses;

public class Commands implements CommandExecutor {
	private IPCheck ipcheck;

	protected Commands(IPCheck ipcheck) {
		this.ipcheck = ipcheck;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ipc")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					sendHelp(player);
					return true;
				}
				if (args[0].equalsIgnoreCase("recent")) {
					List<String> recentFinds = ipcheck.getRecentFinds();
					player.sendMessage(ChatColor.AQUA + StringUtils.join(recentFinds, "\n "));
					return true;
				} else if (args[0].equalsIgnoreCase("lookup")) {
					if(args.length != 2) return false;
					if(InetAddresses.isInetAddress(args[1])) {
						player.sendMessage(ipcheck.getAlts(player.getName(), args[1]));
						return true;
					} else {
						if(Bukkit.getPlayer(args[1]) == null) {
							player.sendMessage(ChatColor.RED + args[1] + ChatColor.GOLD + " is not online!");
							return true;
						} else {
							player.sendMessage(ChatColor.GREEN + "Known Alts: " + ipcheck.getAlts(args[1], 
										Bukkit.getPlayer(args[1]).getAddress().getAddress().getHostAddress())
										);
							return true;
						}
					}
				} else if (args[0].equalsIgnoreCase("toggle")) {
					if (ipcheck.getStaffNotify().contains(player.getName())) {
						ipcheck.getStaffNotify().remove(player.getName());
						player.sendMessage(ChatColor.AQUA + "Alt Notification"
								+ ChatColor.RED + " disabled");
					} else {
						ipcheck.getStaffNotify().add(player.getName());
						player.sendMessage(ChatColor.AQUA + "Alt Notification"
								+ ChatColor.GREEN + " enabled");
					}
					return true;
				} else {
					sendHelp(player);
					return true;
				}
			} else {
				sender.sendMessage("Sorry, players only.");
				return true;
			}
		}
		return false;
	}

	public void sendHelp(Player player) {
		player.sendMessage(ChatColor.GREEN 
		    + "/ipc recent  " + ChatColor.GRAY
				+ "See the recent alt IP logs (since server restart/24hrs)"
				+ ChatColor.GREEN + "\n/ipc toggle  " + ChatColor.GRAY
				+ "Toggle the notification message for a possible alt account"
				+ ChatColor.GREEN + "\n/ipc lookup [player/IP] " + ChatColor.GRAY
				+ "Find recent alt logs for a specific (online) player or IP address");
	}
}
