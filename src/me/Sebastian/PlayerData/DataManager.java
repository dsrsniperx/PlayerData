package me.Sebastian.PlayerData;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager{
	
	Main main;
	
	public DataManager(Main main){
		this.main = main;
	}
	
	
	public void createFolder(){
		
		File folder = new File(main.getDataFolder()+File.separator+"playerdata");
		
		if(!folder.exists()){
			folder.mkdirs();
		}
	}
	
	FileConfiguration playerFile = null;
	File file = null;
	
	public void reloadPlayerlist() {
		if (playerFile == null) {
			file = new File(main.getDataFolder(), "playerlist.yml");
		}
		playerFile = YamlConfiguration.loadConfiguration(file);
	}

	public FileConfiguration getPlayerList() {
		if (playerFile == null) {
			reloadPlayerlist();
		}
		return playerFile;
	}

	public void savePlayerList() {
		if ((playerFile == null) || (file == null)) {
			return;
		}
		try {
			getPlayerList().save(file);
		} catch (IOException ex) {
			this.main.getLogger().log(Level.SEVERE,
					"Could not save config to " + file, ex);
		}
	}
	
	public FileConfiguration getPlayerFile(UUID playerUUID){
		File file = new File(main.getDataFolder()+File.separator+"playerdata", playerUUID.toString()+".yml");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
		
		return playerFile;
	}
	public FileConfiguration getPlayerFile(String playerName){
		UUID playerUUID = getUUID(playerName);
		return getPlayerFile(playerUUID);
	}
	
	public void savePlayerFile(UUID playerUUID, FileConfiguration file){
		try {
			file.save(new File(main.getDataFolder()+File.separator+"playerdata", playerUUID.toString()+".yml"));
		} catch (IOException ex) {
			this.main.getLogger().log(Level.SEVERE,
					"Could not save config to " + file, ex);
		}
	}
	public void savePlayerFile(String playerName, FileConfiguration file){
		UUID playerUUID = getUUID(playerName);
		savePlayerFile(playerUUID, file);
	}
	
	public boolean hasFile(UUID playerUUID){
		File file = new File(main.getDataFolder()+File.separator+"playerdata", playerUUID.toString()+".yml");
		return file.exists();
	}
	public boolean hasFile(String playerName){
		UUID playerUUID = getUUID(playerName);
		return hasFile(playerUUID);
	}
	@SuppressWarnings("deprecation")
	public UUID getUUID(String playerName){
		for(String uuid : getPlayerList().getConfigurationSection("").getKeys(false)){
			String name = getPlayerList().getString(uuid);
			if(name.equalsIgnoreCase(playerName)){
				return UUID.fromString(uuid);
			}
		}
		
		return Bukkit.getOfflinePlayer(playerName).getUniqueId();
	}
}
