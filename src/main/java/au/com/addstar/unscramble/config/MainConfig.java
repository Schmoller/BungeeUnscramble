package au.com.addstar.unscramble.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;

public class MainConfig extends YamlConfig
{
	public MainConfig(File file)
	{
		CONFIG_FILE = file;
	}
	
	@Path("random-words")
	public final List<String> words = new ArrayList<>();
	
	@Path("display-answer-on-failed-games")
	public final boolean displayAnswer = true;
	
	@Path("auto-game-enabled")
	public final boolean autoGameEnabled = false;

	@Path("expiring-prize-warning-days")
	@Comment("The time threshold for warning players of expiring prizes")
	public final int expiringPrizeWarningDays = 5;

	@Path("prize-expiration-days")
	@Comment("Unclaimed prizes more than this number of days old are deleted")
	public final int prizeExpirationDays = 28;

	@Path("claim-servers")
	@Comment("The names of servers players may claim their rewards on")
	public final List<String> claimServers = new ArrayList<>();
}
