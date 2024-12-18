package au.com.addstar.unscramble;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.Prizes;
import au.com.addstar.unscramble.prizes.SavedPrize;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnscrambleCommand extends Command
{
	public UnscrambleCommand()
	{
		super("unscramble", null, "us");
	}
	
	private boolean permTest(CommandSender sender, String perm)
	{
		if(!sender.hasPermission(perm))
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to perform that command."));
			return false;
		}
		
		return true;
	}
	
	@Override
	public void execute( CommandSender sender, String[] args )
	{
		if(args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			commandHelp(sender);
		}
		else if(args[0].equalsIgnoreCase("claim"))
		{
			commandClaim(sender);
		}
		else if(args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("points"))
		{
			commandStats(sender);
		}
		else if(args[0].equalsIgnoreCase("reload"))
		{
			if(!permTest(sender, "unscramble.reload"))
				return;

			commandReload(sender);
		}
		else if(args[0].equalsIgnoreCase("hint"))
		{
			if(!permTest(sender, "unscramble.hint"))
				return;

			commandHint(sender);
		}
		else if(args[0].equalsIgnoreCase("cancel"))
		{
			if(!permTest(sender, "unscramble.cancel"))
				return;

			commandCancel(sender);
		}
		else if(args[0].equalsIgnoreCase("newgame"))
		{
			if(!permTest(sender, "unscramble.newgame"))
				return;

			commandNewgame(sender, Arrays.copyOfRange(args, 1, args.length));
		}
		else if(args[0].equalsIgnoreCase("debug"))
		{
			if(!permTest(sender, "unscramble.debug"))
				return;

			Unscramble.setDebug(!Unscramble.getDebug());
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Debug mode is now " + (Unscramble.getDebug() ? "enabled" : "disabled")));
		}
		else
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Unknown command /unscramble " + args[0]));
		}
	}
	
	private void commandHelp(CommandSender sender)
	{
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "=================" + ChatColor.RED + " [ Unscramble Help ] " + ChatColor.DARK_PURPLE + "=================="));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "help " + ChatColor.YELLOW + "- Shows this screen"));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "stats " + ChatColor.YELLOW + "- Show your unscramble stats"));
		if(sender.hasPermission("unscramble.claim"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "claim " + ChatColor.YELLOW + "- Claims any prizes you have won"));

		if(sender.hasPermission("unscramble.reload"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "reload " + ChatColor.YELLOW + "- Reloads the config"));
		
		if(sender.hasPermission("unscramble.hint"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "hint " + ChatColor.YELLOW + "- Gives a hint on the current word"));
		
		if(sender.hasPermission("unscramble.cancel"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "cancel " + ChatColor.YELLOW + "- Cancels any currently running game"));
		
		if(sender.hasPermission("unscramble.newgame")) {
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "newgame w:[word] t:[time] h:[hint-interval] c:[hint-chars] [prize] " + ChatColor.YELLOW + "- Starts a new game with the given details"));
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + " [prize] examples: " + ChatColor.GOLD  + "item diamond 1" + ChatColor.GREEN + " or " + ChatColor.GOLD  + "$150"));
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + " Note: Underscores (_) in [word] will be changed into spaces"));
		}

		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "====================================================="));
	}
	
	private void commandClaim(CommandSender sender)
	{
		if(sender instanceof ProxiedPlayer)
		{
			final ProxiedPlayer player = (ProxiedPlayer)sender;

			List<String> claimServers = Unscramble.instance.getConfig().claimServers;
			if(!claimServers.isEmpty() && !claimServers.contains(player.getServer().getInfo().getName()))
			{
				StringBuilder serverString = new StringBuilder();
				for(int i = 0; i < claimServers.size(); ++i)
				{
					String server = claimServers.get(i);
					
					if(i != 0)
					{
						if(i != claimServers.size()-1)
							serverString.append(", ");
						else
							serverString.append(" or ");
					}
					
					serverString.append(server);
				}
				
				if(claimServers.size() > 1)
					serverString.insert(0, "either ");
					
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "You may not claim your prizes here. Please go to " + serverString));
				return;
			}

			int MAX_PRIZES_TO_AWARD = 10;

			List<SavedPrize> prizes = Unscramble.instance.getPrizes(player, true, MAX_PRIZES_TO_AWARD);
			if(prizes == null || prizes.isEmpty())
			{
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "No prizes found under your name."));
				return;
			}

			// Keep track of the number of prizes awarded
			// Allow the player to claim up to 10 at a time to avoid too many simultaneous sessions or
			// spamming the user with "Your inventory was full" messages if they run out of room
			int prizesAwarded = 0;

			for(SavedPrize prize : prizes)
			{
				int session = prize.prize.award(player);
				Unscramble.instance.startPrizeSession(session, player, prize.prize, prize.entered);

				if(++prizesAwarded >= MAX_PRIZES_TO_AWARD) {
					if(prizesAwarded < prizes.size()) {

						// More prizes remain; inform the player
						// Using a 2 second delay to prevent the message from appearing before the messages regarding awarded prizes

						Runnable task = () -> player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "You have more prizes to claim. Make sure you have free inventory space then use " + ChatColor.GOLD + "/us claim" + ChatColor.DARK_AQUA + " again"));

						Unscramble.instance.getProxy().getScheduler().schedule(Unscramble.instance, task, 2, TimeUnit.SECONDS);

					}
					break;
				}
			}
		}
		else
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "No prizes found under your name."));
	}

	private void commandStats(CommandSender sender) {
		UUID uuid;
		ProxiedPlayer player;
		if (sender instanceof ProxiedPlayer) {
			player = (ProxiedPlayer) sender;
			uuid = player.getUniqueId();
		} else {
			sender.sendMessage(ChatColor.RED + "This command must be run by a player");
			return;
		}
		DatabaseManager dbm = Unscramble.instance.getDatabaseManager();
		DatabaseManager.PlayerRecord rec = dbm.getRecord(uuid);
		sender.sendMessage(ChatColor.YELLOW + "Unscramble stats for " + player.getDisplayName() + ":");
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&l► &aTotal Wins: &b" + rec.getWins()));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&l► &aTotal Points Earned: &b" + rec.getTotalPoints()));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&l► &6Point Balance: &b" + rec.getPoints()));
	}

	private void commandReload(CommandSender sender)
	{
		Unscramble.instance.reload();
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Reloaded unscramble configs"));
	}
	
	private void commandHint(CommandSender sender)
	{
		if(!Unscramble.instance.isSessionRunning())
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "There is no unscramble game running."));
			return;
		}
		
		Unscramble.instance.getSession().doHint();
	}
	
	private void commandCancel(CommandSender sender)
	{
		if(!Unscramble.instance.isSessionRunning())
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "There is no unscramble game running."));
			return;
		}
		
		Unscramble.instance.getSession().stop();
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Game cancelled."));
	}
	
	private void commandNewgame(CommandSender sender, String[] args)
	{
		if(Unscramble.instance.isSessionRunning())
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "There is already an unscramble game running."));
			return;
		}
		
		String word = "";
		int hints = 0;
		int hintChars = 2;
		int time = 30000;
		Prize prize = null;
		
		for(int i = 0; i < args.length; ++i)
		{
			String arg = args[i].toLowerCase();
			
			if(arg.startsWith("w:"))
				word = arg.substring(2).replace('_', ' ');
			else if(arg.startsWith("t:"))
			{
				try
				{
					if(arg.substring(2).endsWith("s")) {
						// User entered something like t:30s
						time = Integer.parseInt(arg.substring(2, arg.length() - 1));
					} else {
						time = Integer.parseInt(arg.substring(2));
					}

					if(time <= 0)
					{
						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Time must be 1 or greater (seconds)"));
						return;
					}
					
					time *= 1000;
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Time must be 1 or greater (seconds)"));
					return;
				}
			}
			else if(arg.startsWith("h:"))
			{
				try
				{
					if(arg.substring(2).endsWith("s")) {
						// User entered something like h:10s
						hints = Integer.parseInt(arg.substring(2, arg.length() - 1));
					} else {
						hints = Integer.parseInt(arg.substring(2));
					}

					if(hints <= 0)
					{
						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Hint interval must be 1 or greater (seconds)"));
						return;
					}
					
					hints *= 1000;
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Hint interval must be 1 or greater (seconds)"));
					return;
				}
			}
			else if(arg.startsWith("c:"))
			{
				try
				{
					hintChars = Integer.parseInt(arg.substring(2));

					if(hintChars <= 0)
					{
						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Number of chars to reveal per hint"));
						return;
					}

				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Hint-chars interval must be 1 or greater"));
					return;
				}
			}
			else
			{
				StringBuilder prizeString = new StringBuilder();
				for(int j = i; j < args.length; ++j)
				{
					if(prizeString.length() > 0)
						prizeString.append(" ");
					prizeString.append(args[j]);
				}
				
				try
				{
					prize = Prizes.parse(prizeString.toString());
					if(prize == null)
					{
						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Cannot create prize from '" + prizeString + "'. Please check your typing."));
						return;
					}
				}
				catch(IllegalArgumentException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + e.getMessage()));
					return;
				}
				
				break;
			}
		}
		
		Unscramble.instance.newSession(word, time, hints, hintChars, prize);
	}

}
