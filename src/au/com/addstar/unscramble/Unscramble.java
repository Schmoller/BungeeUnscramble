package au.com.addstar.unscramble;

import java.util.Random;

import net.md_5.bungee.api.plugin.Plugin;

public class Unscramble extends Plugin
{
	public static Random rand = new Random();
	public static Unscramble instance;
	
	
	@Override
	public void onEnable()
	{
		instance = this;
		getProxy().getPluginManager().registerCommand(this, new NewGameCommand());
	}
	
	@Override
	public void onDisable()
	{
	}
	
}
