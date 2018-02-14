package au.com.addstar.unscramble.prizes;

import net.cubespace.Yamler.Config.InvalidConverterException;
import net.cubespace.Yamler.Config.YamlConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SavedPrize extends YamlConfig
{
	private SavedPrize()
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

	public SavedPrize(String player, Prize prize, String entered)
	{
		this();

		this.player = player;
		this.prize = prize;
		this.entered = entered;
	}

	public String player;
	public Prize prize;
	public String entered;
}
