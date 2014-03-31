package au.com.addstar.unscramble.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.PrizeSaver;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConverterException;

public class UnclaimedPrizes extends Config
{
	public UnclaimedPrizes(File file)
	{
		try
		{
			addConverter(PrizeSaver.class);
		}
		catch ( InvalidConverterException e )
		{
			e.printStackTrace();
		}
		CONFIG_FILE = file;
		
		prizes = new HashMap<String, List<Prize>>();
	}
	
	public HashMap<String, List<Prize>> prizes;
}
