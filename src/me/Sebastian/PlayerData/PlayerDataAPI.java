package me.Sebastian.PlayerData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerDataAPI extends DataManager{
	
	public static Main main;
	
	public PlayerDataAPI(Main instance){
		super(instance);
		main = instance;
		pDataAPI = this;
	}
	
	public static PlayerDataAPI pDataAPI;
	
	public final int maxPartyMembers = 4;
	
	public static PlayerDataAPI getAPI(){
		if(pDataAPI == null){
			pDataAPI = new PlayerDataAPI(Main.getMainClass());
		}
		return pDataAPI;
	}
	
	public void checkConnection(){
		main.dataBase.checkConnection();
	}
	public Connection getConnection(){
		checkConnection();
		return main.connection;
	}
	public FileConfiguration getConfig(){
		return main.getConfig();
	}
	public void save(){
		main.saveConfig();
	}
	
	///////////////////////////////////////////////////////////////////
	// General SQL Utils TODO
	///////////////////////////////////////////////////////////////////
	
	public void createTable(final String table, final String initialKey) throws SQLException{
		checkConnection();
		if(!containsTable(table)){
			new Thread(new Runnable() {
				public void run() {
					try {
						PreparedStatement sampleQueryStatement = main.connection
								.prepareStatement("CREATE TABLE "
										+ table
										+ " ("+initialKey+" VARCHAR(255));");
						
						sampleQueryStatement.executeUpdate();
						sampleQueryStatement.close();
						
					} catch (SQLException e){
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	public boolean containsTable(String table){
		checkConnection();
		try{
			DatabaseMetaData md2 = main.connection.getMetaData();
			ResultSet rsTables = md2.getColumns(null, null, table, null);
			
			boolean value = rsTables.next();
			rsTables.close();
			return value;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public boolean containsColumn(String table, String key){
		//key = key.replace('.', '_');
		checkConnection();
		try{
			DatabaseMetaData md2 = main.connection.getMetaData();
			ResultSet rsTables = md2.getColumns(null, null, table, key);
			
			boolean value = rsTables.next();
			rsTables.close();
			return value;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public void createColumn(String table, String column){
		String newColumn = column.replace('.', '_').replace('-', '_');
		//column = column.replace('.', '_');
		checkConnection();
		
		if(!containsTable(table)){
			return;
		}
		if(containsColumn(table, newColumn)){
			return;
		}
		try{
			
			PreparedStatement statement = main.connection.prepareStatement("ALTER TABLE `"+table+"` ADD "+newColumn+" VARCHAR(255);");
			statement.executeUpdate();
			
			statement.close();
			
		}catch(Exception e){
			
			e.printStackTrace();
			main.logger.severe(table+" : "+newColumn);
		}
	}
	public void removeColumn(String table, String column){
		checkConnection();
		if(containsColumn(table, column)){
			try{
				
				PreparedStatement statement = main.connection.prepareStatement("ALTER TABLE `"+table+"` DROP `"+column+"`;");
				statement.executeUpdate();
				
				statement.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
//	public List<String> getColumns(String table){
//		List<String> columns = new ArrayList<>();
//		
//		try{
//			PreparedStatement statement = main.connection.prepareStatement("SELECT * FROM `"+table+"`");
//			ResultSet result = statement.executeQuery();
//			ResultSetMetaData rsmd = result.getMetaData();
//			for(int i = 1; i < rsmd.getColumnCount(); i++){
//				String columnName = rsmd.getColumnLabel(i);
//				if(columnName != null){
//					columns.add(columnName);
//				}
//			}
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return columns;
//	}
	
	/////////////////////////////////////////////////////////////////////////
	// Player Data MYSQL TODO
	////////////////////////////////////////////////////////////////////////
	
	public void resetPlayer(String playerName){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("DELETE FROM `player_data` WHERE name=?;");
			statement.setString(1, playerName);
			statement.executeUpdate();
			
			statement.close();
			
			statement = main.connection.prepareStatement("DELETE FROM `permissions` WHERE name=?;");			
			statement.setString(1, playerName);
			statement.executeUpdate();
			
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		getConfig().set(playerName, null);
		save();
	}
	public boolean containsPlayer(UUID playerUUID){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("SELECT * FROM `"+main.mainTable+"` WHERE uuid=?;");
			statement.setString(1, playerUUID.toString());
			ResultSet result = statement.executeQuery();
			
			boolean value = result.next();
			result.close();
			statement.close();
			
			return value;
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	
	}
	
	public void addPlayer(final UUID playerUUID){
		checkConnection();
		
		if(containsPlayer(playerUUID)){
			return;
		}
		try{
			PreparedStatement columns = main.connection.prepareStatement("SELECT * FROM `player_data`;");
			
			ResultSet rs = columns.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			String values = "?, ";
			if(columnCount <= 1){
				values = "?";
			}else{
				for(int i = 2; i <= columnCount; i++){
					if(i >= columnCount){
						values = values+"NULL";
					}else{
						values = values+"NULL, ";
					}
				}
			}
			
			PreparedStatement statement = main.connection.prepareStatement("INSERT INTO `"+main.mainTable+"` values("+values+");");
			statement.setString(1, playerUUID.toString());
			statement.executeUpdate();
			statement.close();
			columns.close();
			rs.close();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void setValue(final UUID uuid, final String key, final Object value){
		new BukkitRunnable(){
			public void run(){
				try{
					checkConnection();
					
					if(!containsColumn(main.mainTable, key)){
						createColumn("player_data", key);
					}
					PreparedStatement statement = main.connection.prepareStatement("UPDATE `"+main.mainTable+"` SET "+key+"=? WHERE uuid=?;");
					statement.setString(1, value.toString());
					statement.setString(2, uuid.toString());
					statement.executeUpdate();
					
					statement.close();
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}.runTaskAsynchronously(main);
	}
	public String getString(final UUID uuid, String key){
		checkConnection();
		String value = null;
		
		if(!containsColumn(main.mainTable, key)){
			createColumn("player_data", key);
		}
		
		try{
			if(containsColumn(main.mainTable, key)){
				PreparedStatement statement = main.connection.prepareStatement("SELECT "+key+" FROM `"+main.mainTable+"` WHERE uuid=?;");
				statement.setString(1, uuid.toString());
				ResultSet result = statement.executeQuery();
				
				if(result.next()){
					value = result.getString(key);
				}
				result.close();
				statement.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return value;
	}
	public int getInt(UUID uuid, String key){
		try{
			int value = Integer.parseInt(getString(uuid, key));
			return value;
		}catch(Exception e){
			
		}
		return 0;
	}
	public boolean getBoolean(UUID uuid, String key){
		try{
			boolean value = Boolean.parseBoolean(getString(uuid, key));
			return value;
		}catch(Exception e){
			
		}
		return false;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Party MYSQL TODO
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public void addPartyLeader(final String leaderName){
		checkConnection();
		if(isPartyLeader(leaderName)){
			return;
		}
		
		try{
			PreparedStatement columns = main.connection.prepareStatement("SELECT * FROM `party`;");
			
			ResultSet rs = columns.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			
			String values = "?, ";
			if(columnCount == 0){
				createColumn("party", "leader");
			}
			if(columnCount == 1){
				values = "?";
			}else{
				for(int i = 2; i <= columnCount; i++){
					if(i >= columnCount){
						values = values+"NULL";
					}else{
						values = values+"NULL, ";
					}
				}
			}
			
			PreparedStatement mapInsert = main.connection.prepareStatement("INSERT INTO `party` values("+values+");");
			mapInsert.setString(1, leaderName);
			mapInsert.executeUpdate();
			
			mapInsert.close();
			columns.close();
			rs.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public void removePartyLeader(final String leaderName){
		checkConnection();
		if(!isPartyLeader(leaderName)){
			return;
		}
		
		try{
			PreparedStatement statement = main.connection.prepareStatement("DELETE FROM `party` WHERE leader=?;");
			statement.setString(1, leaderName);
			
			statement.executeUpdate();
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public boolean isPartyLeader(String leaderName){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("SELECT * FROM `party` WHERE leader=?;");
			statement.setString(1, leaderName);
			ResultSet result = statement.executeQuery();
			
			boolean value = result.next();
			result.close();
			statement.close();
			return value;
		}catch(Exception e){
			return false;
		}
	}
	public boolean isPartyMember(String name){
		checkConnection();
		try{
			
			for(int i = 1; i <= 3; i++){
				PreparedStatement statement = main.connection.prepareStatement("SELECT * FROM `party` WHERE member_"+i+"=?;");
				statement.setString(1, name);
				
				ResultSet result = statement.executeQuery();
				
				if(result.next()){
					result.close();
					return true;
				}
				result.close();
				statement.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	public boolean isInParty(String name){
		return isPartyLeader(name) || isPartyMember(name);
	}
	public List<String> getPartyMembers(String leaderName, boolean includeLeader){
		checkConnection();
		List<String> members = new ArrayList<>();
		if(includeLeader){
			members.add(leaderName);
		}
		try{
			for(int i = 1; i <= maxPartyMembers; i++){
				PreparedStatement statement = main.connection.prepareStatement("SELECT member_"+i+" FROM `party` WHERE leader=?;");
				statement.setString(1, leaderName);
				ResultSet result = statement.executeQuery();
				
				if(result.next()){
					String value = result.getString("member_"+i);
					if(value != null){
						if(!value.equalsIgnoreCase("null")){
							if(!members.contains(value)){
								members.add(value);
							}
						}
					}					
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return members;
	}
	public String getPartyLeader(String member){
		checkConnection();
		if(isPartyLeader(member)){
			return member;
		}
		String leader = null;
		try{
			if(!isPartyMember(member)){
				return leader;
			}
			PreparedStatement statement = main.connection.prepareStatement("SELECT leader FROM `party` WHERE member_1=? OR member_2=? OR member_3=?;");
			statement.setString(1, member);
			statement.setString(2, member);
			statement.setString(3, member);
			ResultSet result = statement.executeQuery();
			if(result.next()){
				leader = result.getString("leader");
			}
			result.close();
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return leader;
	}
	
	public void setPartyValue(final String leaderName, final String key, final String value){
		checkConnection();
		new BukkitRunnable(){
			public void run(){
				try{
					if(!isPartyLeader(leaderName)){
						PreparedStatement columns = main.connection.prepareStatement("SELECT * FROM `party`;");
						
						ResultSet rs = columns.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						
						
						String values = "?, ";
						if(columnCount == 1){
							values = "?";
						}else{
							for(int i = 2; i <= columnCount; i++){
								if(i >= columnCount){
									values = values+"NULL";
								}else{
									values = values+"NULL, ";
								}
							}
						}
						
						
						PreparedStatement mapInsert = main.connection.prepareStatement("INSERT INTO `party` values("+values+");");
						mapInsert.setString(1, leaderName);
						mapInsert.executeUpdate();
						
						mapInsert.close();
						columns.close();
						rs.close();
					}
					if(!containsColumn("party", key)){
						createColumn("party", key);
					}
					PreparedStatement statement = main.connection.prepareStatement("UPDATE `party` SET "+key+"=? WHERE leader=?;");
					statement.setString(1, value);
					statement.setString(2, leaderName);
					statement.executeUpdate();
					
					statement.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(main);
		
	}
	public String getPartyValue(String leaderName, String key){
		checkConnection();
		String value = null;
		
		try{
			if(!isPartyLeader(leaderName)){
				return value;
			}
			try{
				if(!containsColumn("party", key)){
					return value;
				}
				PreparedStatement statement = main.connection.prepareStatement("SELECT "+key+" FROM `party` WHERE leader=?;");
				statement.setString(1, leaderName);
				ResultSet result = statement.executeQuery();
				
				if(result.next()){
					value = result.getString(key);
				}
				result.close();
				statement.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return value;
		}catch(Exception e){
			
		}
		
		return value;
	}
	
	public boolean hasTipped(String playerName){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("SELECT * FROM `tips` WHERE name=?;");
			statement.setString(1, playerName);
			
			ResultSet result = statement.executeQuery();
			if(result.next()){
				return true;
			}
			result.close();
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public void addTipper(String playerName){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("INSERT INTO `tips` values(?);");
			statement.setString(1, playerName);
			statement.executeUpdate();
			
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void clearTipList(){
		checkConnection();
		try{
			PreparedStatement statement = main.connection.prepareStatement("TRUNCATE `tips`");
			statement.executeUpdate();
			
			statement.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
