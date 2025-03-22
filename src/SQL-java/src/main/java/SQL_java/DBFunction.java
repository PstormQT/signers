package SQL_java;

import com.jcraft.jsch.*;

import java.lang.Thread.State;
import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This is our class to access and interact with the Database. 
 * Any method/function that directly accesses the Database will and should be in here
 *      with VERY FEW EXCEPTIONS
 */
public class DBFunction{

    private Connection connection;
    private java.util.Date utilDate = new java.util.Date();
    private java.sql.Date currentDate;
    public static final String DBNAME = "p32001_05";
    private Session session;

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
            this.session = jsch.getSession(user, rhost, 22);
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

    public Connection getConnection(){
        return connection;
    }

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
        Statement statement;
        ResultSet results;
        try{
        String query = "SELECT * FROM users WHERE username='"+username+"' AND password='"+password+"'";
        statement = this.connection.createStatement();
        results = statement.executeQuery(query);
        if (results.next()){
            query = "UPDATE users SET last_login_date = ? WHERE username = ? AND password = ?";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setDate(1, currentDate);
            pdst.setString(2, username);
            pdst.setString(3, password);
            pdst.executeUpdate();
            return new User(results.getInt("user_id"), results.getString("username"),
                                   results.getString("password"));
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
        ResultSet results;
        try{
            String query = "INSERT INTO users (password,creation_date,last_login_date,email,username,fname,lname) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement pdst = connection.prepareStatement(query);
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
                try{
                    String query2 = "SELECT user_id,username,password FROM users WHERE username = ? AND password = ?";
                    PreparedStatement pdstII = connection.prepareStatement(query2);
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
     * Searches music collections by name and prints out list
     * @param name
     * @param User
     * @return List of all collection with that name from that user
     * @author Andrew Rosenhaus
     */
    public ArrayList<MusicCollection> collectionSearch(String name, User user){
        ResultSet results = null;
        String query = "SELECT name, number_of_songs, total_time, mc_id, user_id FROM music_collection WHERE LOWER(name) = ? AND user_id = ? ORDER BY name ASC";
        try(PreparedStatement pdst = connection.prepareStatement(query)) {
            pdst.setString(1, name);
            pdst.setInt(2, user.getId());
            results = pdst.executeQuery();
            ArrayList<MusicCollection> returnList = new ArrayList<>();
            if(!results.next()){
                System.out.println( "No results found for query.");
                return null;
            }
            while (results.next())
                for (int j = 1; j <= 3; j++){
                    System.out.print(results.getString(j) + ", ");
                }
                returnList.add(new MusicCollection(results.getString("name"), results.getInt("total_time"), results.getInt("number_of_songs"), results.getInt("mc_id"), results.getInt("user_id")));
                System.out.println("");
                results.next();
                return returnList;
        }
        catch (SQLException e){
            System.out.println(e);
            return null;
        }
        finally {
                try{
                    if (results != null){
                        results.close();
                    }
                }
                catch (SQLException c){
                    System.out.println(c);
                    return null;
                }
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
    public MusicCollection createCollection(String name, Integer total_time, Integer number_of_songs, Integer user_id){
        String query = "INSERT INTO music_collection (name, total_time, number_of_songs, user_id) VALUES (?,?,?,?)" ;
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setString(1, name);
        pdst.setInt(2, total_time);
        pdst.setInt(3, number_of_songs);
        pdst.setInt(4, user_id);
        int rowsAffected = pdst.executeUpdate();
        
        if (rowsAffected == 1){
            ResultSet results;
            try{
                String query2 = "SELECT mc_id, name, total_time, number_of_songs, user_id FROM music_collection WHERE name = ?";
                PreparedStatement pdstII = connection.prepareStatement(query2);
                pdstII.setString(1, name);
                results = pdstII.executeQuery();
                if (results.next()){
                    return new MusicCollection(results.getString("name"), results.getInt("total_time"), results.getInt("number_of_songs"), results.getInt("mc_id"), results.getInt("user_id"));
                }
                else{return null;}
            }
            catch(SQLException er){
                System.out.println(er);
                return null;
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
            query = "UPDATE music_collection SET total_time = ?, number_of_songs = ? WHERE mc_id = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(1, total_time);
            pdstII.setInt(2, number_of_songs);
            pdstII.setInt(3, mc_id);
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
            query = "UPDATE music_collection SET total_time = ?, number_of_songs = ? WHERE mc_id = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(1, total_time);
            pdstII.setInt(2, number_of_songs);
            pdstII.setInt(3, mc_id);
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
     * Updates name of collection
     * @param name
     * @param updatedName
     * @returns true if successful, false if error
     * @author Andrew Rosenhaus
     */
    public boolean modifyCollectionName(MusicCollection collection, String updatedName) {
        ResultSet results = null;
        String query = "UPDATE music_collection SET name = ? WHERE mc_id = ?";
        try(PreparedStatement pdst = connection.prepareStatement(query)) {
            pdst.setString(1, updatedName);
            pdst.setInt(2, collection.getMCId());
            results = pdst.executeQuery();
            return true;
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
        finally {
                try{
                    if (results != null){
                        results.close();
                    }
                }
                catch (SQLException c){
                    System.out.println(c);
                    return false;
                }
            }
    }

    /**
     * Deletes collection
     * @param mc_id
     * @return true if successful, false if error
     * @author Andrew Rosenhaus
     */

    public boolean deleteCollection(MusicCollection collection) {
        ResultSet results = null;

        try {
            String query = "DELETE FROM music_collection WHERE mc_id = ?";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setInt(1, collection.getMCId());
            pdst.executeQuery();
            return true;
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
        finally {
                try{
                    if (results != null){
                        results.close();
                    }
                }
                catch (SQLException c){
                    System.out.println(c);
                    return false;
                }
            }
    }


    /**
     * Closes the connection with the DB server. 
     * MAKE SURE TO ALWAYS CALL THIS AT END OF MAIN
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
        User testUser = test.login("MasterFaster", "RDA");
        System.out.println(testUser);
        MusicCollection collection1 = test.createCollection("Test", 0, 0, testUser.getId());
        MusicCollection collection2 = test.createCollection("Test", 0, 0, testUser.getId());
        test.collectionSearch("Test", testUser);
        test.modifyCollectionName(collection1, "Update");
        test.deleteCollection(collection2);
        System.out.println(test.closeConnection());
    }
}