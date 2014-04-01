package au.com.addstar.unscramble;

import java.util.Arrays;
import java.util.List;

import au.com.addstar.unscramble.prizes.ItemPrize;
import au.com.addstar.unscramble.prizes.Prize;

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
		if(args.length == 0)
		{
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "====================================================="));
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + " Welcome to " + ChatColor.RED + "Unscramble " + ChatColor.GREEN + "Plugin " + ChatColor.BLUE + "(" + Unscramble.instance.getDescription().getVersion() + ")"));
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "====================================================="));
		}
		else
		{
			if(args[0].equalsIgnoreCase("help"))
			{
				commandHelp(sender);
			}
			else if(args[0].equalsIgnoreCase("claim"))
			{
				commandClaim(sender);
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
			else
			{
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Unknown command /unscramble " + args[0]));
			}
		}
	}
	
	private void commandHelp(CommandSender sender)
	{
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "=================" + ChatColor.RED + " [ Unscramble Help ] " + ChatColor.DARK_PURPLE + "=================="));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.YELLOW + "- States the general info."));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "claim " + ChatColor.YELLOW + "- Claims any prizes you have won."));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "help " + ChatColor.YELLOW + "- Shows this screen."));
		
		if(sender.hasPermission("unscramble.reload"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "reload " + ChatColor.YELLOW + "- Reloads the config."));
		
		if(sender.hasPermission("unscramble.hint"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "hint " + ChatColor.YELLOW + "- Gives a hint on the current word."));
		
		if(sender.hasPermission("unscramble.cancel"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "cancel " + ChatColor.YELLOW + "- Cancels any currently running game."));
		
		if(sender.hasPermission("unscramble.newgame"))
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/unscramble " + ChatColor.GRAY + "newgame w:[word] p:<prize> a:<amount> t:<time> h:<hint-interval> " + ChatColor.YELLOW + "- Starts a new game with the given details"));
		
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Note: Underscores (_) will be changed into spaces."));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "====================================================="));
	}
	
	private void commandClaim(CommandSender sender)
	{
		if(sender instanceof ProxiedPlayer)
		{
			ProxiedPlayer player = (ProxiedPlayer)sender;
			
			List<Prize> prizes = Unscramble.instance.getPrizes(player, true);
			if(prizes == null || prizes.isEmpty())
			{
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_RED + "No prizes found under your name."));
				return;
			}
			
			for(Prize prize : prizes)
			{
				int session = prize.award(player);
				Unscramble.instance.startPrizeSession(session, player, prize);
			}
		}
		else
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_RED + "No prizes found under your name."));
	}
	
	private void commandReload(CommandSender sender)
	{
		
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
		int time = 30000;
		
		for(int i = 0; i < args.length; ++i)
		{
			String arg = args[i].toLowerCase();
			
			if(arg.startsWith("w:"))
				word = arg.substring(2).replace('_', ' ');
			else if(arg.startsWith("t:"))
			{
				try
				{
					time = Integer.parseInt(arg.substring(2));
					if(time <= 0)
					{
						sender.sendMessage(TextComponent.fromLegacyText("Time must be 1 or greater"));
						return;
					}
					
					time *= 1000;
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText("Time must be number 1 or greater"));
					return;
				}
			}
			else if(arg.startsWith("h:"))
			{
				try
				{
					hints = Integer.parseInt(arg.substring(2));
					if(hints <= 0)
					{
						sender.sendMessage(TextComponent.fromLegacyText("Hint interval must be 1 or greater"));
						return;
					}
					
					hints *= 1000;
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(TextComponent.fromLegacyText("Hint interval must be number 1 or greater"));
					return;
				}
			}
		}
		
		Unscramble.instance.newSession(word, time, hints, new ItemPrize("SMOOTH_BRICK", 3, 20));
	}

}
