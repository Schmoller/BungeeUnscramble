package au.com.addstar.unscramble.prizes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class Prize
{
	static int nextSession = 0;
	
	public abstract String getDescription();
	public abstract int award(ProxiedPlayer player);
	
	public abstract Entry<Prize, String> handleFail(DataInputStream input) throws IOException;
	
	public abstract Map<String, Object> save();
}
