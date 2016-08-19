package me.Sebastian.PlayerData;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBase {
	Main main;

	public DataBase(Main instance) {
		main = instance;
	}
	
	boolean loginError = false;
	
	public void checkConnection(){
		if(loginError){
			return;
		}
		if(main.connection == null){
			establishMySQLConnection();
			return;
		}
		try{
			if(main.connection.isClosed()){
				establishMySQLConnection();
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
			establishMySQLConnection();
		}
		
	}
	public synchronized void establishMySQLConnection() {
		try {
			System.out.println("[Player_Data]"
					+ "Establishing secure `MySQL` Connection...");
			main.connection = DriverManager.getConnection(
					"jdbc:mysql://" + main.host + ":" + main.port + "/"
							+ main.database + "?autoreconnect=true",
					main.username, main.password);
			System.out.println("[Player_Data]"
					+ "Established secure `MySQL` Connection!");
			System.out.println("[Player_Data]"
					+ "Checking if table exists, if not creating one.");
			if (!hasCorrectTable()) {
				//createTable();
				System.out.println("[Player_Data]"
								+ "Table missing!?! Creating one for you with correct variables. ;)");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Could not connect to MYSQL Database!!!");
			loginError = true;
			//main.getServer().shutdown();
		}
	}
	
	public void createTable() throws SQLException {
		try {
			String values = "(";
			for(int i = 0; i < main.mainTableValues.size(); i++){
				String value = main.mainTableValues.get(i);
				
				if(i == main.mainTableValues.size()-1){
					values = values+value+" VARCHAR(255)";
				}else{
					values = values+value+" VARCHAR(255), ";
				}
			}
			
			values = values+")";
			System.out.println("[Player_Data] Values: "+values);
			
			PreparedStatement sampleQueryStatement = main.connection
					.prepareStatement("CREATE TABLE "
							+ main.mainTable
							+ " "
							+ values);//"(uuid VARCHAR (255), username VARCHAR(255), forum VARCHAR(255), ipaddress VARCHAR(255), name VARCHAR(255), address VARCHAR(255), phone VARCHAR(255), email VARCHAR(255), onlineTime INTEGER(255), rank VARCHAR(255), sex VARCHAR(255), computerdesc VARCHAR(255), jokes VARCHAR(255))");
			
			sampleQueryStatement.executeUpdate();
			sampleQueryStatement.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Boolean hasCorrectTable() throws SQLException {
		DatabaseMetaData dbm = main.connection.getMetaData();
		ResultSet rs = dbm.getTables(null, null, main.mainTable, null);
		if (rs.next()) {
			return true;
		}
		return false;
	}
}
