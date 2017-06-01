package me.Sebastian.PlayerData;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConnectionHandler implements Listener{

	Main main;
	
	public ConnectionHandler(Main instance){
		main = instance;
		
		
	}
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		
		Player player = event.getPlayer();
		UUID playerUUID = player.getUniqueId();

		FileConfiguration file = main.api.getPlayerFile(playerUUID);
		if(file.getBoolean("General.SQL")){
			return;
		}
		file.set("General.SQL", true);
		main.api.savePlayerFile(playerUUID, file);		
		
		main.api.addPlayer(playerUUID);
		
	}
}
