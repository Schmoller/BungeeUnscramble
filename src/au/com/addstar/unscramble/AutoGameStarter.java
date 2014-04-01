package au.com.addstar.unscramble;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class AutoGameStarter implements Runnable
{
	private int mWarning;
	private int mMinPlayers;
	
	public AutoGameStarter(int warningTime, int minPlayers)
	{
		mWarning = warningTime;
		mMinPlayers = minPlayers;
	}
	
	private boolean canRun()
	{
		if(BungeeCord.getInstance().getOnlineCount() >= mMinPlayers)
			return true;
		else
		{
			Unscramble.instance.getLogger().info("Not running auto game as there is not enough people on");
			return false;
		}
	}
	
	@Override
	public void run()
	{
		if(!canRun())
			return;
		
		if(mWarning == 0)
			Unscramble.instance.startAutoGame();
		else
		{
			BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.YELLOW + "A game of unscramble is about to start!"));
			BungeeCord.getInstance().getScheduler().schedule(Unscramble.instance, new Runnable()
			{
				@Override
				public void run()
				{
					if(!canRun())
						return;
					Unscramble.instance.startAutoGame();
				}
			}, mWarning, TimeUnit.SECONDS);
		}
	}
}
