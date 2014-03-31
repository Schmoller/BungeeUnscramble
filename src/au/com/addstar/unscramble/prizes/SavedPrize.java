package au.com.addstar.unscramble.prizes;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConverterException;

public class SavedPrize extends Config
{
	public SavedPrize()
	{
		try
		{
			addConverter(PrizeSaver.class);
		}
		catch(InvalidConverterException e)
		{
			e.printStackTrace();
		}
	}
	
	public SavedPrize(String player, Prize prize)
	{
		this();
		
		this.player = player;
		this.prize = prize;
	}
	
	public String player;
	public Prize prize;
}
