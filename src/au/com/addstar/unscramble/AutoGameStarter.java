package au.com.addstar.unscramble;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class AutoGameStarter implements Runnable
{
	private int mWarning = 0;
	
	public AutoGameStarter(int warningTime)
	{
		mWarning = warningTime;
	}
	
	@Override
	public void run()
	{
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
					Unscramble.instance.startAutoGame();
				}
			}, mWarning, TimeUnit.SECONDS);
		}
	}
}
