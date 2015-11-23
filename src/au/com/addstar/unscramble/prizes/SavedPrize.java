package au.com.addstar.unscramble.prizes;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConverterException;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		this.entered = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	public String player;
	public Prize prize;
	public String entered;
}
