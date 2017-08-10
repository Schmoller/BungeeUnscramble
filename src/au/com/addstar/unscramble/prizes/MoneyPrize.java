package au.com.addstar.unscramble.prizes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import au.com.addstar.unscramble.MessageOutput;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MoneyPrize extends Prize
{
	private double mAmount;
	
	public MoneyPrize(double amount)
	{
		mAmount = amount;
	}
	
	@Override
	public String getDescription()
	{
		return String.format("$%.2f", mAmount);
	}

	@Override
	public int award( ProxiedPlayer player )
	{
		int session = nextSession++;
		new MessageOutput("Unscramble", "Award$")
			.writeInt(session)
			.writeUTF(player.getName())
			.writeDouble(mAmount)
			.send(player.getServer().getInfo());
		
		return session;
	}
	
	@Override
	public Entry<Prize, String> handleFail( DataInputStream input ) throws IOException
	{
		// Doesnt happen
		return null;
	}

	@Override
	public Map<String, Object> save()
	{
		HashMap<String, Object> map = new HashMap<>();
		map.put("type", "money");
		map.put("amount", mAmount);
		return map;
	}
	
	public static MoneyPrize load(Map<String, Object> map)
	{
		return new MoneyPrize(((Number)map.get("amount")).doubleValue());
	}
}
