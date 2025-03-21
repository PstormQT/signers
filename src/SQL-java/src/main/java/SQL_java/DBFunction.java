package SQL_java;

import java.lang.Thread.State;
import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import org.apache.commons.lang3.ObjectUtils.Null;

import java.sql.Date;
import java.sql.PreparedStatement;

/**
 * This is our class to access and interact with the Database. 
 * Any method/function that directly accesses the Database will and should be in here
 *      with VERY FEW EXCEPTIONS
 */
public class DBFunction{

    private Connection connection;
    private java.util.Date utilDate = new java.util.Date();
    private java.sql.Date currentDate;

    public Connection getConnection(){
        return connection;
    }

    public DBFunction() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(abc.DBLink, abc.USERNAME, abc.PASSWORD);
            if (connection == null) {
                throw new Exception("Error connecting to the database");
            }
            currentDate = new Date(utilDate.getTime());
        } catch (Exception e) {
            System.err.println(e);
        }
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
        ResultSet results;
        try{
        String query = "SELECT user_id,username,password FROM users WHERE username=? AND password=?";
        PreparedStatement pdst = connection.prepareStatement(query);
        pdst.setString(1, username);
        pdst.setString(2, password);
        results = pdst.executeQuery();
        if (results.next()){
            query = "UPDATE users SET last_login_date = ? WHERE username = ? AND password = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setDate(1, currentDate);
            pdstII.setString(2, username);
            pdstII.setString(3, password);
            pdstII.executeUpdate();
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
     * Closes the connection with the DB server. 
     * MAKE SURE TO ALWAYS CALL THIS AT END OF MAIN
     * @return true if close was successful, false if otherwise
     */
    public boolean closeConnection(){
        try{
            connection.close();
            return true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
    }


    public ArrayList<Song> searchSongs(String containedText, String order){
        if (order == null){
            //odrer by title, then artist name (first artist name alphabetically)
            order = "TITLE, (SELECT )";
        }
        String query = "SELECT ";


        return null;

    }
    

    /**
     * Listens to a specific song with a particular ID
     * And also updates the listen count of the song
     * @param songID The ID of the song we are listening to 
     * @param user The user that is listening to the song
     * @return The Number of rows changed
     * @author Antonio Bicknell <acb9430>
     */
    public int listenToSong(int songID, User user){
        String query = "INSERT INTO listens_to (list_user_id, list_song_id, date_time_listened) VALUES (?,?,?)";
        String updateQuery = "UPDATE song SET playcount = playcount + 1 WHERE song_id = ?";
        try(PreparedStatement insertST = connection.prepareStatement(query);
        PreparedStatement updateST = connection.prepareStatement(updateQuery)  ){
        
        insertST.setInt(1, user.getId());
        insertST.setInt(2, songID);
        insertST.setTimestamp(3, Timestamp.from(Instant.now()) );
        
        
        updateST.setInt(1, songID);
        int rowsAffected = 0;
        rowsAffected += insertST.executeUpdate();
        rowsAffected += updateST.executeUpdate();



        return rowsAffected;
        }
        catch(SQLException e){
            System.err.println(e);
            return 0;
            }
        

    }

    /**
     * 
     * @param colID the ID of the Collection we are listening to
     * @param user The User listening to the collection
     * @return Number of rows changed
     * @author Antonio Bicknell <acb9430>
     */
    public int listenToCollection(int colID, User user){
        String insertQuery = "INSERT INTO listens_to (list_user_id,  list_song_id, date_time_listened) SELECT ?, song_id, ? FROM collection_song WHERE mc_id = ?";
        String updatequery = "UPDATE song SET playcount = playcount + 1 WHERE song_id IN (SELECT song_id FROM collection_song WHERE mc_id = ?)";
        try(PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            PreparedStatement updateStatement = connection.prepareStatement(updatequery)   ){
                insertStatement.setInt(1, user.getId());
                insertStatement.setTimestamp(2, Timestamp.from(Instant.now()) );
                insertStatement.setInt(3, colID);

                updateStatement.setInt(1, colID);

                int rowsAffected = 0;
                rowsAffected += insertStatement.executeUpdate();
                rowsAffected += updateStatement.executeUpdate();
                return rowsAffected;

            }
             catch (SQLException e) {
                System.out.println(e);
                return 0;
            }

    }


    
    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        User testUser = test.login("MasterFaster", "RDA");
        System.out.println(testUser);
        System.out.println(test.closeConnection());
    }


}