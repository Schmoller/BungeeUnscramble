package au.com.addstar.unscramble;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import au.com.addstar.unscramble.config.GameConfig;
import au.com.addstar.unscramble.config.MainConfig;
import au.com.addstar.unscramble.config.UnclaimedPrizes;
import au.com.addstar.unscramble.prizes.Prize;
import au.com.addstar.unscramble.prizes.SavedPrize;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class Unscramble extends Plugin implements Listener
{
	public static Random rand = new Random();
	public static Unscramble instance;

	private Session mCurrentSession = null;
	private GameConfig mAutoGame;
	private ScheduledTask mAutoGameTask;
	private MainConfig mConfig;

	private UnclaimedPrizes mUnclaimed;

	private SimpleDateFormat mDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private HashMap<Integer, SavedPrize> mActiveSessions = new HashMap<Integer, SavedPrize>();

	@Override
	public void onEnable()
	{
		instance = this;

		getDataFolder().mkdirs();

		getProxy().getPluginManager().registerCommand(this, new UnscrambleCommand());
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().registerChannel("Unscramble");

		mConfig = new MainConfig(new File(getDataFolder(), "config.yml"));
		mUnclaimed = new UnclaimedPrizes(new File(getDataFolder(), "unclaimed.yml"));

		reload();
	}

	@Override
	public void onDisable()
	{
	}

	private void loadAutoGame()
	{
		if(mAutoGameTask != null)
			mAutoGameTask.cancel();
		mAutoGameTask = null;

		mAutoGame = new GameConfig();
		try
		{
			mAutoGame.init(new File(getDataFolder(), "auto.yml"));
			mAutoGame.initialize();

			if(mConfig.autoGameEnabled)
			{
				getLogger().info("Starting AutoGame timer. Will only run with at least " + mAutoGame.minPlayers + " players online.");
				mAutoGameTask = getProxy().getScheduler().schedule(this, new AutoGameStarter(mAutoGame.warningPeriod, mAutoGame.minPlayers), mAutoGame.interval, mAutoGame.interval, TimeUnit.MINUTES);
			}
		}
		catch(InvalidConfigurationException e)
		{
			mAutoGame = null;
			System.err.println("Could not load auto game: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void reload()
	{
		try
		{
			mConfig.init();
			mUnclaimed.init();
		}
		catch(InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}

		loadAutoGame();
	}

	@EventHandler
	public void onPlayerChat(ChatEvent event)
	{
		if(event.getSender() instanceof ProxiedPlayer)
		{
			ProxiedPlayer player = (ProxiedPlayer)event.getSender();

			if(mCurrentSession != null)
				mCurrentSession.makeGuess(player, event.getMessage());
		}
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {

		if(event.getPlayer() instanceof ProxiedPlayer)
		{
			ProxiedPlayer player = event.getPlayer();

			List<SavedPrize> prizes = getPrizes(player, false, 0);

			if(prizes != null && !prizes.isEmpty()) {
				NotifyPlayerUnclaimedPrizes(player, prizes);
			}
		}

	}

	public void onSessionFinish()
	{
		mCurrentSession = null;
	}

	public void newSession(String word, long length, long hintInterval, Prize prize)
	{
		if(mCurrentSession != null)
			throw new IllegalStateException("Session in progress");

		Session session = new Session(word, length, hintInterval, prize);
		session.start();
		mCurrentSession = session;
	}

	public void startAutoGame()
	{
		if(mAutoGame == null || mCurrentSession != null)
			return;

		Session session = mAutoGame.newSession();
		if(session == null)
			return;

		session.start();
		mCurrentSession = session;
	}

	public Session getSession()
	{
		return mCurrentSession;
	}

	public boolean isSessionRunning()
	{
		return mCurrentSession != null;
	}

	public void givePrize(ProxiedPlayer player, Prize prize)
	{
		mUnclaimed.prizes.add(new SavedPrize(player.getName(), prize));

		// Check for and remove expired prizes
		removeExpiredPrizes();

		try
		{
			mUnclaimed.save();
		}
		catch ( InvalidConfigurationException e )
		{
			e.printStackTrace();
		}
	}

	public List<SavedPrize> getPrizes(ProxiedPlayer player, boolean remove, int maxPrizesToRemove)
	{
		if(remove)
		{
			List<SavedPrize> prizes = mUnclaimed.getPrizes(player.getName(), true, maxPrizesToRemove);
			try
			{
				mUnclaimed.save();
			}
			catch(InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			return prizes;
		}
		else
			return mUnclaimed.getPrizes(player.getName(), false, maxPrizesToRemove);
	}

	public void startPrizeSession(int sessionId, ProxiedPlayer player, Prize prize, String entered)
	{
		mActiveSessions.put(sessionId, new SavedPrize(player.getName(), prize, entered));
	}

	public String getRandomWord()
	{
		if(mConfig.words.isEmpty())
			return "unscramble";

		return mConfig.words.get(rand.nextInt(mConfig.words.size()));
	}

	public MainConfig getConfig()
	{
		return mConfig;
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event)
	{
		if(!event.getTag().equals("Unscramble"))
			return;

		ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
		DataInputStream input = new DataInputStream(stream);

		try
		{
			String subChannel = input.readUTF();
			int session = input.readInt();

			SavedPrize prize = mActiveSessions.remove(session);
			ProxiedPlayer player = getProxy().getPlayer(prize.player);

			if(subChannel.equals("AwardFail"))
			{
				byte hasMoreData = input.readByte();
				if(hasMoreData == 2)
				{
					mUnclaimed.prizes.add(prize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "An unknown error occured giving you your prizes. Please notify an admin"));
					getLogger().severe("Could not award prize: " + prize.prize.getDescription() + " (" + prize.prize.getClass().getSimpleName() + "). It was rejected by '" + player.getServer().getInfo().getName() + "'. Check that this type is handled by that server.");
				}
				else if(hasMoreData == 1)
				{
					Entry<Prize, String> result = prize.prize.handleFail(input);
					SavedPrize newPrize = new SavedPrize(prize.player, result.getKey(), prize.entered);
					mUnclaimed.prizes.add(newPrize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + result.getValue()));
				}
				else
				{
					mUnclaimed.prizes.add(prize);
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.RED + "You can not claim the prize " + prize.prize.getDescription() + " in this location."));
				}

				try
				{
					mUnclaimed.save();
				}
				catch(InvalidConfigurationException e)
				{
					e.printStackTrace();
				}
			}
			else if(subChannel.equals("AwardOk"))
			{
				getProxy().getPlayer(prize.player).sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "[Unscramble] " + ChatColor.DARK_AQUA + "You have been awarded " + ChatColor.YELLOW + prize.prize.getDescription()));
				getLogger().info("Awarded prize to " + prize.player + ": " + prize.prize.getDescription());
			}
		}
		catch(IOException e)
		{
		}
	}

	private Date ParseDate(String dateValue) {

		try {
			Date parsedDate = mDateParser.parse(dateValue);
			return parsedDate;

		} catch (ParseException ex) {
			return null;
		}

	}

	private void NotifyPlayerUnclaimedPrizes(final ProxiedPlayer player, List<SavedPrize> prizes) {

		final int prizeCount = prizes.size();

		// Determine the oldest and newest prizes
		long currentTimeMillis = System.currentTimeMillis();
		long earliestPrizeEntered = currentTimeMillis;
		long newestPrizeEntered = 0;

		long prizeExpirationDays = mConfig.prizeExpirationDays;

		// Do not allow prizes less than one week old to expire
		if(prizeExpirationDays < 7)
			prizeExpirationDays = 7;

		long expirationTimeMillis = currentTimeMillis - prizeExpirationDays * 86400 * 1000;

		for(SavedPrize prize : prizes) {

			if(prize.entered == null || prize.entered.isEmpty()) {
				prize.entered = mDateParser.format(currentTimeMillis);
			}

			Date prizeDate = ParseDate(prize.entered);

			if(prizeDate != null) {

				if(prizeDate.getTime() > expirationTimeMillis) {
					long prizeDateMillis = prizeDate.getTime();

					if(prizeDateMillis < earliestPrizeEntered) {
						earliestPrizeEntered = prizeDateMillis;
					}

					if(prizeDateMillis > newestPrizeEntered) {
						newestPrizeEntered = prizeDateMillis;
					}
				}
			}

		}

		if(newestPrizeEntered < currentTimeMillis - prizeExpirationDays * 86400 * 1000) {
			// All of the player's prizes are expired
			return;
		}

		// Warn the player if any prizes will expire soon
		long expiredPrizeWarnThreshold = currentTimeMillis - (prizeExpirationDays - mConfig.expiringPrizeWarningDays) * 86400 * 1000;

		final String expirationMessage;
		if(earliestPrizeEntered < expiredPrizeWarnThreshold) {

			if(prizeCount > 1)
				expirationMessage = ChatColor.RED + "Warning: " + ChatColor.GREEN + "You have prizes expiring soon!";
			else
				expirationMessage = ChatColor.RED + "Warning: " + ChatColor.GREEN + "Prize expires soon!";

		} else {
			expirationMessage = "";
		}

		Runnable task = new Runnable() {
			@Override
			public void run() {

				String prizeMessage;

				if(prizeCount > 1)
					prizeMessage = ChatColor.GOLD + Integer.toString(prizeCount) + ChatColor.GREEN + " unclaimed prizes. ";
				else
					prizeMessage = ChatColor.GOLD + "1" + ChatColor.GREEN + " unclaimed prize. ";

				player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "[Unscramble] " + ChatColor.GREEN + "You have " + prizeMessage + "Use " + ChatColor.GOLD + "/us claim"));

				if(!expirationMessage.isEmpty())
					player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "[Unscramble] " + expirationMessage));

			}
		};

		getProxy().getScheduler().schedule(this, task, 5, TimeUnit.SECONDS);
	}

	public void removeExpiredPrizes()
	{

		try
		{
			long prizeExpirationDays = mConfig.prizeExpirationDays;
			if(prizeExpirationDays < 7)
				prizeExpirationDays = 7;

			long expirationTimeMillis = System.currentTimeMillis() - prizeExpirationDays * 86400 * 1000;

			getLogger().info("Looking for expired unclaimed prizes; count = " + mUnclaimed.prizes.size());

			Iterator<SavedPrize> it = mUnclaimed.prizes.iterator();

			while(it.hasNext())
			{
				SavedPrize prize = it.next();

				if(prize.entered == null || prize.entered.isEmpty()) {
					prize.entered = mDateParser.format(System.currentTimeMillis());
				}

				Date prizeDate = ParseDate(prize.entered);

				if(prizeDate != null && prizeDate.getTime() < expirationTimeMillis) {
					getLogger().info("Removing expired prize for " + prize.player + ", awarded " + prize.entered + "; " + prize.prize.getDescription());
					it.remove();
				}
			}

		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}
