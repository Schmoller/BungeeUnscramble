package au.com.addstar.unscramble.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import au.com.addstar.unscramble.Session;
import au.com.addstar.unscramble.Unscramble;
import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.Prizes;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.InvalidConverterException;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;

public class GameConfig extends YamlConfig
{
	private static WeightedPrizeSaver mSaver = new WeightedPrizeSaver(null);
	
	public GameConfig()
	{
		wordList = new ArrayList<>();
		prizes = new ArrayList<>();
		prizes.add(new WeightedPrize(2, "$10"));
		prizes.add(new WeightedPrize(1, "$20"));
		
		try
		{
			addConverter(WeightedPrizeSaver.class);
		}
		catch(InvalidConverterException e)
		{
			e.printStackTrace();
		}
	}
	
	@Comment("The time between games in minutes. This time includes the length of the game, so it is the real interval.")
	public int interval = 15;
	
	@Comment("The length of the game in seconds")
	public int length = 30;
	
	@Comment("The warning period (in seconds) given so people have time to get ready")
	@Path("warning-period")
	public int warningPeriod = 3;
	
	@Path("hint-interval")
	@Comment("The time between each hint given in seconds. 0 will disable hints")
	public int hintInterval = 12;
	
	@Path("min-players")
	@Comment("The minimum number of players online needed to run this game")
	public int minPlayers = 3;
	
	@Path("words")
	@Comment("The words the game can draw from. You can have as many words as you want and they may include spaces. Leave empty to use the normal random word list")
	public List<String> wordList;
	
	@Comment("The prizes that will be given out.")
	public List<WeightedPrize> prizes;
	
	private ArrayList<Entry<Integer, Prize>> mWeightedPrizes;
	private int mTotal;
	
	public Session newSession()
	{
		if(wordList.isEmpty())
			return new Session("", length * 1000, hintInterval * 1000, getPrize());
		
		return new Session(wordList.get(Unscramble.rand.nextInt(wordList.size())), length * 1000, hintInterval * 1000, getPrize());
	}
	
	@Override
	protected boolean doSkip( Field field )
	{
		return (Modifier.isPrivate(field.getModifiers()));
	}
	
	public void initialize()
	{
		// Because the loader doesnt try other converters when loading lists, this is to put everything as the right type
		try
		{
			for(int i = 0; i < prizes.size(); ++i)
				prizes.set(i, (WeightedPrize)mSaver.fromConfig(null, prizes.get(i), null));
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		mWeightedPrizes = new ArrayList<>();
		
		mTotal = 0;
		for(WeightedPrize prize : prizes)
		{
			try
			{
				mTotal += prize.weight;
				Prize p = Prizes.parse(prize.prize);
				
				if(p == null)
				{
					Unscramble.instance.getLogger().severe(String.format("Error loading %s. Error in prize '%d:%s' Unable to parse prize", CONFIG_FILE.getName(), prize.weight, prize.prize));
					continue;
				}
				
				mWeightedPrizes.add(new AbstractMap.SimpleEntry<>(mTotal, p));
			}
			catch(IllegalArgumentException e)
			{
				Unscramble.instance.getLogger().severe(String.format("Error loading %s. Error in prize '%d:%s' %s", CONFIG_FILE.getName(), prize.weight, prize.prize, e.getMessage()));
			}
		}
	}
	
	public Prize getPrize()
	{
		if(mWeightedPrizes.isEmpty())
			return null;
		
		int val = Unscramble.rand.nextInt(mTotal);
		
		int index = Collections.binarySearch(mWeightedPrizes, new AbstractMap.SimpleEntry<>(val, null), new Comparator<Entry<Integer, Prize>>()
		{
			@Override
			public int compare( Entry<Integer, Prize> o1, Entry<Integer, Prize> o2 )
			{
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		if(index < 0)
			index = (index + 1) * -1;
		
		return mWeightedPrizes.get(index).getValue();
	}
	
	
}
