package au.com.addstar.unscramble;

import java.util.HashSet;
import java.util.Random;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Unscramble extends Plugin implements Listener
{
	public static Random rand = new Random();
	public static Unscramble instance;
	
	private HashSet<Session> mCurrentSessions = new HashSet<Session>();
	
	@Override
	public void onEnable()
	{
		instance = this;
		getProxy().getPluginManager().registerCommand(this, new NewGameCommand());
		getProxy().getPluginManager().registerListener(this, this);
	}
	
	@Override
	public void onDisable()
	{
	}
	
	@EventHandler
	public void onPlayerChat(ChatEvent event)
	{
		if(event.getSender() instanceof ProxiedPlayer)
		{
			ProxiedPlayer player = (ProxiedPlayer)event.getSender();
			
			for(Session session : mCurrentSessions)
			{
				session.makeGuess(player, event.getMessage());
			}
		}
	}
	
	public void onSessionFinish(Session session)
	{
		mCurrentSessions.remove(session);
	}
	
	public void newSession(String word, long length)
	{
		Session session = new Session(word, length);
		session.start();
		mCurrentSessions.add(session);
	}
	
}
