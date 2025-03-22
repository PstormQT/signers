package SQL_java;

import com.jcraft.jsch.*;

import java.lang.Thread.State;
import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collection;
import java.sql.Date;
import java.sql.PreparedStatement;

/**
 * This is our class to access and interact with the Database. 
 * Any method/function that directly accesses the Database will and should be in here
 *      with VERY FEW EXCEPTIONS
 * NOTICE: DISCONNECT FROM DATAGRIP IN THE ORDER FOR THE CODE TO WORK
 */
public class DBFunction{

    private Connection connection;
    private java.util.Date utilDate = new java.util.Date();
    private java.sql.Date currentDate;
    private Session session = null;
    public static final String DBNAME = "p32001_05";

    /**
     * Return the current connection to the database
     * @return
     */
    public Connection getConnection(){
        return connection;
    }

    /**
     * Contructor for the connection to the database
     * Added SSH tunneling to the server
     */
    public DBFunction() {
        this.currentDate = new java.sql.Date(utilDate.getTime());
        
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = abc.USERNAME; //change to your username
        String password = abc.PASSWORD; //change to your password
        String databaseName = DBNAME; //change to your database name

        String driverName = "org.postgresql.Driver";
        
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            this.connection = DriverManager.getConnection(url, props);

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /*
    public ResultSet selectExec(PreparedStatement pdst){
        if(connection == null){
            try {
                Class.forName("org.postgresql.Driver");
                this.connection = DriverManager.getConnection(abc.DBLink, abc.USERNAME, abc.PASSWORD);
                if (connection == null) {
                    throw new Exception("Error connecting to the database");
                }
                currentDate = new Date(utilDate.getTime());
                return pdst.executeQuery();
            } catch (Exception e) {
                System.err.println(e);
                return null;
            }
        }
        else{
            try{
                return pdst.executeQuery();
            }
            catch (Exception e){
                return null;
            }
        }

    }
        */

        /* 
    public int updateExec(PreparedStatement pdst){
        if(connection == null){
            try {
                Class.forName("org.postgresql.Driver");
                this.connection = DriverManager.getConnection(abc.DBLink, abc.USERNAME, abc.PASSWORD);
                if (connection == null) {
                    throw new Exception("Error connecting to the database");
                }
                currentDate = new Date(utilDate.getTime());
                return pdst.executeUpdate();
            } catch (Exception e) {
                System.err.println(e);
                return 0;
            }
        }
        else{
            try{
                return pdst.executeUpdate();
            }
            catch (Exception e){
                return 0;
            }
        }
    }
        */


    /**
     * Testing the connection to the server
     * @param table_name
     */
    public void testAccessData(String table_name){
        Statement statement;
        ResultSet results;
        try {
            String query = "SELECT * FROM " + table_name ;
            statement = this.connection.createStatement();
            results = statement.executeQuery(query);
            for (int i = 0; i < 10; i++){
                results.next();
                System.out.println(results.getString("username"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    /**
     * Retrieves an existing User's id, username, and password, and creates a User object
     * We were going to have a class for this, but anything that accesses the DB should be done in this class
     *  for ease of use and simplicity
     * @param username  The User's username
     * @param password  The User's password
     * @return          A User object if an existing user with the credentials is found. Null if otherwise
     * @author Brandon Yi
     */
    public User login(String username, String password){
        ResultSet results = null;
        String query = "SELECT user_id,username,password FROM users WHERE username=? AND password=?";
        try( PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setString(1, username);
        pdst.setString(2, password);
        results = pdst.executeQuery();
        if (results.next()){
            query = "UPDATE users SET last_login_date = ? WHERE username = ? AND password = ?";
            try(PreparedStatement pdstII = connection.prepareStatement(query);){
                pdstII.setDate(1, currentDate);
                pdstII.setString(2, username);
                pdstII.setString(3, password);
                pdstII.executeUpdate();    
                return new User(results.getInt("user_id"), results.getString("username"),
                                    results.getString("password"));
            }
        }
        else{
            return null;
        }
        }
        catch (SQLException e) {
            System.out.println(e);
            return null;
        }
       
    }

    /**
     * Creates a collection for the user
     * @param name  the name of the collection
     * @param total_time   the total time of the songs in the collection 
     * @param number_of_songs   the total amount of songs in the collection
     * @param user_id   the id of the user making the collection
     * @return  a boolean if the collection was created successfully or not 
     * @author Katie Richardson
     */
    public boolean createCollection(String name, Integer total_time, Integer number_of_songs, Integer user_id){
        String query = "INSERT INTO music_collection (name, total_time, number_of_songs, user_id) VALUES (?,?,?,?)" ;
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setString(1, name);
        pdst.setInt(2, total_time);
        pdst.setInt(3, number_of_songs);
        pdst.setInt(4, user_id);
        int rowsAffected = pdst.executeUpdate();
        if (rowsAffected == 1){
            return true;
        }
        else{
            return false;
        }
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Deletes a song from a collection and updates the number of songs and total time for that collection
     * @param song_id  the ID of the song/album to be deleted from the collection
     * @param mc_id    the ID of the collection
     * @param total_time   the total time of the songs in the collection 
     * @param number_of_songs   the total amount of songs in the collection
     * @return  a boolean if the collection was created successfully or not 
     * @author Katie Richardson
     */
    public boolean deleteSongFromCollection(Integer song_id, Integer mc_id, Integer total_time, Integer number_of_songs){
        String query = "DELETE FROM collection_song WHERE song_id = ? AND mc_id = ?";
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setInt(1, song_id);
        pdst.setInt(2, mc_id);
        int rowsAffected = pdst.executeUpdate();
        if (rowsAffected == 1){
            query = "UPDATE mc_id SET total_time = ?, number_of_songs = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(3, total_time);
            pdstII.setInt(4, number_of_songs);
            pdstII.executeUpdate();
            return true;
        }
        else{
            return false;
        }
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Adds a song to a collection and updates the number of songs and total time for that collection
     * @param song_id  the ID of the song/album to be deleted from the collection
     * @param mc_id    the ID of the collection
     * @param total_time   the total time of the songs in the collection 
     * @param number_of_songs   the total amount of songs in the collection
     * @return  a boolean if the collection was created successfully or not 
     * @author Katie Richardson
     */
    public boolean addSongToCollection(Integer song_id, Integer mc_id, Integer total_time, Integer number_of_songs){
        String query = "INSERT INTO collection_song (song_id, mc_id) VALUES (?,?)";
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setInt(1, song_id);
        pdst.setInt(2, mc_id);
        int rowsAffected = pdst.executeUpdate();
        if (rowsAffected == 1){
            query = "UPDATE mc_id SET total_time = ?, number_of_songs = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(3, total_time);
            pdstII.setInt(4, number_of_songs);
            pdstII.executeUpdate();
            return true;
        }
        else{
            return false;
        }
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }


    /**
     * Creates a User object based on Parameters
     * Username and Email are unique, so it WILL NOT create new rows with duplicates of those
     * @param username
     * @param password
     * @param fname
     * @param lname
     * @param email
     * @return A user if successfully created, null if otherwise
     */
    public User createUser(String username, String password, String fname, String lname, String email){
        ResultSet results = null;
        String query = "INSERT INTO users (password,creation_date,last_login_date,email,username,fname,lname) VALUES (?,?,?,?,?,?,?)";
        try(PreparedStatement pdst = connection.prepareStatement(query);){
            pdst.setString(1, password);
            pdst.setDate(2, currentDate);
            pdst.setDate(3, currentDate);
            pdst.setString(4, email);
            pdst.setString(5, username);
            pdst.setString(6, fname);
            pdst.setString(7, lname);
            int rowsAffected = pdst.executeUpdate();
            System.out.println(rowsAffected);
            if(rowsAffected == 1){
                String query2 = "SELECT user_id,username,password FROM users WHERE username = ? AND password = ?";
                try(PreparedStatement pdstII = connection.prepareStatement(query2);){
                    pdstII.setString(1, username);
                    pdstII.setString(2, password);
                    results = pdstII.executeQuery();
                    if (results.next()){
                        return new User(results.getInt("user_id"), results.getString("username"),
                                               results.getString("password"));
                    }
                    else{return null;}
                }
                catch(SQLException er){
                    System.out.println(er);
                    return null;
                }
            }
            else{return null;}
    
        }
        catch(SQLException e){
            System.out.println(e);
            return null;
        }
    }


    /**
     * Query of looking up the user by email
     * @param email
     * @return the list of the user match the mail - should be 0 or 1 if I'm not dumb
     */
    public ArrayList<User> lookUpByEmail(String email){
        ResultSet data; 
        ArrayList<User> returnData = new ArrayList<>();
        try {
            String query ="SELECT * FROM users WHERE email=?";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setString(1, email);
            data = pdst.executeQuery();
            while (data.next()){
                User user = new User(data.getInt("user_id"), data.getString("username"),
                data.getString("password"));
                returnData.add(user);
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        return returnData;
    }

    /**
     * Follow a user
     * @param user user who request a follow
     * @param following user that got a follow
     * @return if the execution is success, and there is no conflict
     */
    public boolean userFollowing(int user, int following){
        ResultSet data = null;
        PreparedStatement pdst = null;
        PreparedStatement pdst2 = null;
    
    try {
        String query = "SELECT * FROM following WHERE user_id=? AND following_id=?";
        pdst = this.connection.prepareStatement(query);
        pdst.setInt(1, user);
        pdst.setInt(2, following);
        data = pdst.executeQuery();

        if (data.next()) {
            return false;
        }

        String query2 = "INSERT INTO following (user_id, following_id) VALUES(?, ?)";
        pdst2 = this.connection.prepareStatement(query2);
        pdst2.setInt(1, user);
        pdst2.setInt(2, following);

        int rowsAffected = pdst2.executeUpdate();
        return rowsAffected > 0;

    } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }



    /**
     * UnFollow a user (ddel;ete the relation in the DB)
     * @param user user who request an Unfollow
     * @param following user that got an unfollow
     * @return if the execution is success, and there is no conflict
     */
    public boolean userUnFollowing(int user, int following){
        ResultSet data = null;
        PreparedStatement pdst = null;
        PreparedStatement pdst2 = null;
    
    try {
        String query = "SELECT * FROM following WHERE user_id=? AND following_id=?";
        pdst = this.connection.prepareStatement(query);
        pdst.setInt(1, user);
        pdst.setInt(2, following);
        data = pdst.executeQuery();

        if (!data.next()) {
            return false;
        }

        String query2 = "DELETE FROM following WHERE user_id=? AND following_id=?";
        pdst2 = this.connection.prepareStatement(query2);
        pdst2.setInt(1, user);
        pdst2.setInt(2, following);

        int rowsAffected = pdst2.executeUpdate();
        System.out.println(rowsAffected);
        return rowsAffected > 0;

    } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Closes the connection with the DB server. 
     * MAKE SURE TO ALWAYS CALL THIS AT END OF TRANSACTION
     * @return true if close was successful, false if otherwise
     */
    public boolean closeConnection(){
        try{
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("Closing Database Connection");
                this.connection.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
            return true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
    }

    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        // User testUser = test.login("MasterFaster", "RDA");

        System.out.println(test.createCollection("Test", 0, 0, 1));
        System.out.println("");
        System.out.println(test.addSongToCollection(1, 1, 10, 1));
        System.out.println("");
        System.out.println(test.deleteSongFromCollection(1, 1, 0, 0));
        // System.out.println(testUser);
        System.out.println(test.closeConnection());
    }

}