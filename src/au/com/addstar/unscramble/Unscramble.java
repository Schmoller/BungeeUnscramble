package au.com.addstar.unscramble;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import au.com.addstar.unscramble.config.GameConfig;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Unscramble extends Plugin implements Listener
{
	public static Random rand = new Random();
	public static Unscramble instance;
	
	private Session mCurrentSession = null;
	private GameConfig mAutoGame;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		getDataFolder().mkdirs();
		
		getProxy().getPluginManager().registerCommand(this, new UnscrambleCommand());
		getProxy().getPluginManager().registerListener(this, this);
		
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
	
	public void newSession(String word, long length, long hintInterval)
	{
		if(mCurrentSession != null)
			throw new IllegalStateException("Session in progress");
		
		Session session = new Session(word, length, hintInterval);
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
	
}
