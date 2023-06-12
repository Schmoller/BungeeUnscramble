package au.com.addstar.unscramble.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public List<String> words = new ArrayList<>();
	
	@Path("display-answer-on-failed-games")
	public boolean displayAnswer = true;
	
	@Path("auto-game-enabled")
	public boolean autoGameEnabled = false;

	@Path("expiring-prize-warning-days")
	@Comment("The time threshold for warning players of expiring prizes")
	public int expiringPrizeWarningDays = 5;

	@Path("prize-expiration-days")
	@Comment("Unclaimed prizes more than this number of days old are deleted")
	public int prizeExpirationDays = 28;

	@Path("claim-servers")
	@Comment("The names of servers players may claim their rewards on")
	public List<String> claimServers = new ArrayList<>();

	@Path("points-table")
	@Comment("Points reward for each difficulty level (difficulty:points)")
	public List<String> pointsTable = new ArrayList<>();

	@Path("db-url")
	@Comment("Database URL")
	public String dbURL = "jdbc:mysql://localhost:3306/db_name";

	@Path("db-username")
	@Comment("Database username")
	public String dbUsername = "user";

	@Path("db-password")
	@Comment("Database password")
	public String dbPassword = "pass";
}
