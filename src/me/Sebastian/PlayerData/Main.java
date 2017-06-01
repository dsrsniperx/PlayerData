package me.Sebastian.PlayerData;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin{

	public static Main main;
	
	public static Main getMainClass(){
		
		return main;
	}
	
	public final Logger logger = Logger.getLogger("Minecraft");
	PluginDescriptionFile pdfFile = getDescription();
	
	DataBase dataBase = new DataBase(this);
	DataManager dManager = new DataManager(this);
	
	PlayerDataAPI api = new PlayerDataAPI(this);

	Connection connection = null;
	public static String PREFIX = "PlayerData - ";
	public String host = "23.251.159.208";
	public String port = "3306";
	public String database = "zombies";
	public String username = "zombies";
	public String password = ";PTx\"tP53jz]";
	public String mainTable = "player_data";
	  
	public List<String> mainTableValues = Arrays.asList("uuid", "name");
	
	@Override
	public void onEnable(){
		main = this;
		
	    dManager.createFolder();
	    dataBase.establishMySQLConnection();
	    this.createPartyTable();
	    
	    this.getServer().getPluginManager().registerEvents(new ConnectionHandler(this), this);
	    //this.createTipListTable();
	    //TODO
	    
	    logger.info("[PlayerData] is now enabled!");
	 }
	public void onDisable(){
		logger.info("[PlayerData] is now disabled!");
	}
	
	public void createPartyTable(){
		try{
	    	PlayerDataAPI.getAPI().createTable("party", "leader");
	    	for(int i = 1; i <= api.maxPartyMembers; i++){
	    		String column = "member_"+i;
		    	api.createColumn("party", column);
	    	}
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
	
	public void createTipListTable(){
		new BukkitRunnable(){
			public void run(){
				try{
			    	api.createTable("tips", "name");
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			}
		}.runTaskAsynchronously(this);
	}
}
