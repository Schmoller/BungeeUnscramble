package au.com.addstar.unscramble;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

class AutoGameStarter implements Runnable
{
	private final int mWarning;
	private final int mMinPlayers;
	
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
		ProxyServer.getInstance().getScheduler().schedule(Unscramble.instance, () -> {
			// Ensure the next game is always scheduled
			Unscramble.debugMsg("Scheduling next game...");
			Unscramble.instance.scheduleNextGame();
		}, 5, TimeUnit.SECONDS);

		if(!canRun())
			return;

		if(mWarning == 0) {
			Unscramble.debugMsg("Starting auto game...");
			Unscramble.instance.startAutoGame();
		} else {
			Unscramble.debugMsg("Broadcasting game start...");
			Unscramble.broadcast(ChatColor.YELLOW + "A game of unscramble is about to start!");
			ProxyServer.getInstance().getScheduler().schedule(Unscramble.instance, () -> {
                if(!canRun())
                    return;
                Unscramble.instance.startAutoGame();
            }, mWarning, TimeUnit.SECONDS);
		}
	}
}
