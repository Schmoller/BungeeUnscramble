package au.com.addstar.unscramble;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import au.com.addstar.unscramble.config.GameConfig;
import au.com.addstar.unscramble.config.UnclaimedPrizes;
import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.SavedPrize;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Unscramble extends Plugin implements Listener
{
	public static Random rand = new Random();
	public static Unscramble instance;
	
	private Session mCurrentSession = null;
	private GameConfig mAutoGame;
	
	private UnclaimedPrizes mUnclaimed;
	
	private HashMap<Integer, SavedPrize> mActiveSessions = new HashMap<Integer, SavedPrize>();
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		getDataFolder().mkdirs();
		
		getProxy().getPluginManager().registerCommand(this, new UnscrambleCommand());
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().registerChannel("Unscramble");
		
		mUnclaimed = new UnclaimedPrizes(new File(getDataFolder(), "unclaimed.yml"));
		
		try
		{
			mUnclaimed.init();
		}
		catch(InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		
		loadAutoGame();
	}
	
	@Override
	public void onDisable()
	{
	}
	
	private void loadAutoGame()
	{
		mAutoGame = new GameConfig();
		try
		{
			mAutoGame.init(new File(getDataFolder(), "auto.yml"));
			
			getProxy().getScheduler().schedule(this, new AutoGameStarter(mAutoGame.warningPeriod), mAutoGame.interval, mAutoGame.interval, TimeUnit.MINUTES);
		}
		catch(InvalidConfigurationException e)
		{
			mAutoGame = null;
			System.err.println("Could not load auto game: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerChat(ChatEvent event)
	{
		if(event.getSender() instanceof ProxiedPlayer)
		{
			ProxiedPlayer player = (ProxiedPlayer)event.getSender();
			
			if(mCurrentSession != null)
				mCurrentSession.makeGuess(player, event.getMessage());
		}
	}
	
	public void onSessionFinish()
	{
		mCurrentSession = null;
	}
	
	public void newSession(String word, long length, long hintInterval, Prize prize)
	{
		if(mCurrentSession != null)
			throw new IllegalStateException("Session in progress");
		
		Session session = new Session(word, length, hintInterval, prize);
		session.start();
		mCurrentSession = session;
	}
	
	public void startAutoGame()
	{
		if(mAutoGame == null || mCurrentSession != null)
			return;
		
		Session session = mAutoGame.newSession();
		if(session == null)
			return;
		
		session.start();
		mCurrentSession = session;
	}
	
	public Session getSession()
	{
		return mCurrentSession;
	}
	
	public boolean isSessionRunning()
	{
		return mCurrentSession != null;
	}
	
	public void givePrize(ProxiedPlayer player, Prize prize)
	{
		mUnclaimed.prizes.add(new SavedPrize(player.getName(), prize));
		
		try
		{
			mUnclaimed.save();
		}
		catch ( InvalidConfigurationException e )
		{
			e.printStackTrace();
		}
	}
	
	public List<Prize> getPrizes(ProxiedPlayer player, boolean remove)
	{
		if(remove)
		{
			List<Prize> prizes = mUnclaimed.getPrizes(player.getName(), true);
			try
			{
				mUnclaimed.save();
			}
			catch(InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			return prizes;
		}
		else
			return mUnclaimed.getPrizes(player.getName(), false);
	}
	
	public void startPrizeSession(int sessionId, ProxiedPlayer player, Prize prize)
	{
		mActiveSessions.put(sessionId, new SavedPrize(player.getName(), prize));
	}
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent event)
	{
		if(!event.getTag().equals("Unscramble"))
			return;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
		DataInputStream input = new DataInputStream(stream);
		
		try
		{
			String subChannel = input.readUTF();
			int session = input.readInt();
			
			SavedPrize prize = mActiveSessions.remove(session);
			ProxiedPlayer player = getProxy().getPlayer(prize.player);
			
			if(subChannel.equals("AwardFail"))
			{
				byte hasMoreData = input.readByte();
				if(hasMoreData == 2)
				{
					mUnclaimed.prizes.add(prize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "An unknown error occured giving you your prizes. Please notify an admin"));
					getLogger().severe("Could not award prize: " + prize.prize.getDescription() + " (" + prize.prize.getClass().getSimpleName() + "). It was rejected by '" + player.getServer().getInfo().getName() + "'. Check that this type is handled by that server.");
				}
				else if(hasMoreData == 1)
				{
					Entry<Prize, String> result = prize.prize.handleFail(input);
					SavedPrize newPrize = new SavedPrize(prize.player, result.getKey());
					mUnclaimed.prizes.add(newPrize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + result.getValue()));
				}
				else
				{
					mUnclaimed.prizes.add(prize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "You can not claim the prize " + prize.prize.getDescription() + " in this location."));
				}
				
				try
				{
					mUnclaimed.save();
				}
				catch(InvalidConfigurationException e)
				{
					e.printStackTrace();
				}
			}
			else if(subChannel.equals("AwardOk"))
			{
				getProxy().getPlayer(prize.player).sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "You have been awarded " + ChatColor.YELLOW + prize.prize.getDescription()));
			}
		}
		catch(IOException e)
		{
		}	
	}
}
