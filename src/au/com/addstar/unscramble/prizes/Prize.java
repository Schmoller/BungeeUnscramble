package au.com.addstar.unscramble.prizes;

import java.util.Map;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class Prize
{
	public abstract String getDescription();
	public abstract void award(ProxiedPlayer player);
	
	public abstract Map<String, Object> save();
}
