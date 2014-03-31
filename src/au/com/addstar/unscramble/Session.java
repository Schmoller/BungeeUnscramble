package au.com.addstar.unscramble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Session implements Runnable
{
	private String mWord;
	private String mWordScramble;
	
	private long mEndTime;
	private long mLastAnnounce;
	
	private String mHint;
	
	// TODO: Prize
	
	private ScheduledTask mTask;
	
	public Session(String word, long duration)
	{
		mWord = word;
		mEndTime = System.currentTimeMillis() + duration;
		
		mHint = word.replaceAll("[^ ]", "*");
		scramble();
	}
	
	public void start()
	{
		mTask = BungeeCord.getInstance().getScheduler().schedule(Unscramble.instance, this, 0, 1, TimeUnit.SECONDS);
		BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "New Game! Unscramble " + ChatColor.ITALIC + "this: " + ChatColor.RED + mWordScramble));
	}
	
	public void doHint()
	{
		if(!mHint.contains("*"))
			return;
		
		while(true)
		{
			int index = Unscramble.rand.nextInt(mWord.length());
			
			char c = mWord.charAt(index);
			char hintC = mHint.charAt(index);
			
			if(c != ' ' && hintC == '*')
			{
				char[] chars = mHint.toCharArray();
				chars[index] = c;
				mHint = new String(chars);
				break;
			}
		}
		
		BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "Hint!... " + mHint));
	}
	
	public boolean isRunning()
	{
		return mTask != null;
	}
	
	public void makeGuess(ProxiedPlayer player, String guess)
	{
		if(!isRunning())
			return;
		
		if(mWord.equalsIgnoreCase(guess))
		{
			BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "Congratulations " + ChatColor.stripColor(player.getDisplayName()) + "!"));
			mTask.cancel();
			mTask = null;
			// TODO: Prizes
			
			Unscramble.instance.onSessionFinish(this);
		}
	}
	
	@Override
	public void run()
	{
		long left = getTimeLeft();
		
		if(left <= 0)
		{
			BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "Oh! Sorry, you didnt get the word in time!"));
			BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "The answer was... " + ChatColor.RED + mWord));

			mTask.cancel();
			mTask = null;
			
			Unscramble.instance.onSessionFinish(this);
			return;
		}
		
		long sinceLastAnnounce = System.currentTimeMillis() - mLastAnnounce;
		
		boolean announce = false;
		if(sinceLastAnnounce >= 1000 && left <= 5000)
			announce = true;
		else if(sinceLastAnnounce >= 5000 && left <= 20000)
			announce = true;
		else if(sinceLastAnnounce >= 10000 && left <= 30000)
			announce = true;
		else if(sinceLastAnnounce >= 15000 && left <= 60000)
			announce = true;
		else if(sinceLastAnnounce >= 30000)
			announce = true;
		
		if(announce)
		{
			mLastAnnounce = System.currentTimeMillis();
			BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + getTimeLeftString()));
		}
	}
	
	public long getTimeLeft()
	{
		return mEndTime - System.currentTimeMillis();
	}
	
	public String getTimeLeftString()
	{
		long time = getTimeLeft();
		time = (long)Math.ceil(time / 1000D) * 1000;
		
		StringBuffer buffer = new StringBuffer();
		
		long minutes = TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS);
		if(minutes > 0)
		{
			if(minutes == 1)
				buffer.append("1 Minute");
			else
			{
				buffer.append(minutes);
				buffer.append(" Minutes");
			}
			time -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
		}
		
		long seconds = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
		if(seconds > 0)
		{
			if(buffer.length() > 0)
				buffer.append(" ");
			
			if(seconds == 1)
				buffer.append("1 Second");
			else
			{
				buffer.append(seconds);
				buffer.append(" Seconds");
			}
		}
		
		buffer.append(" Left");
		
		return buffer.toString();
	}
	
	private void scramble()
	{
		String[] words = mWord.split(" ");
		
		for(int i = 0; i < words.length; ++i)
		{
			String word = words[i];
			
			if(word.length() <= 1)
				continue;
			
			ArrayList<Character> chars = new ArrayList<Character>(word.length());
			for(int c = 0; c < word.length(); ++c)
				chars.add(word.charAt(c));
			
			while(word.equals(words[i])) // Dont allow the correct word to appear
			{
				Collections.shuffle(chars, Unscramble.rand);
				
				StringBuilder builder = new StringBuilder(word.length());
				for(int c = 0; c < chars.size(); ++c)
					builder.append(chars.get(c));
				word = builder.toString();
			}
			
			words[i] = word;
		}
		
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < words.length; ++i)
		{
			if(i != 0)
				builder.append(" ");
			builder.append(words[i]);
		}
		
		mWordScramble = builder.toString();
		
	}
}
