package au.com.addstar.unscramble;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
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
		return ProxyServer.getInstance().getOnlineCount() >= mMinPlayers;
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
			ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.YELLOW + "A game of unscramble is about to start!"));
			ProxyServer.getInstance().getScheduler().schedule(Unscramble.instance, new Runnable()
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
