package au.com.addstar.unscramble.prizes;

import java.util.HashMap;
import java.util.Map;

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
	public void award( ProxiedPlayer player )
	{
		new MessageOutput("Unscramble", "Award$")
			.writeInt(0)
			.writeUTF(player.getName())
			.writeDouble(mAmount)
			.send(player.getServer().getInfo());
	}

	@Override
	public Map<String, Object> save()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", "money");
		map.put("amount", mAmount);
		return map;
	}
	
	public static MoneyPrize load(Map<String, Object> map)
	{
		return new MoneyPrize(((Number)map.get("amount")).doubleValue());
	}
}
