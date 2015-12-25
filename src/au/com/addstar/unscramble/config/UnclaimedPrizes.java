package au.com.addstar.unscramble.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.PrizeSaver;
import au.com.addstar.unscramble.prizes.SavedPrize;

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
		
		prizes = new ArrayList<SavedPrize>();
	}
	
	public List<SavedPrize> prizes;
	
	public List<SavedPrize> getPrizes(String player, boolean remove, int maxPrizesToRemove)
	{
		ArrayList<SavedPrize> prizes = new ArrayList<SavedPrize>();
		Iterator<SavedPrize> it = this.prizes.iterator();

		int prizesRemoved = 0;

		while(it.hasNext())
		{
			SavedPrize prize = it.next();

			if(prize.player.equals(player))
			{
				prizes.add(prize);
				if(remove && (maxPrizesToRemove < 0 || prizesRemoved < maxPrizesToRemove)) {
					it.remove();
					prizesRemoved++;
				}
			}
		}
		
		return prizes;
	}

}
