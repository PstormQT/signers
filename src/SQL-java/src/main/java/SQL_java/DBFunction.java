package SQL_java;

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
     * Searches music collections by name and prints out list
     * @param name
     * @param User
     * @return List of all collection with that name from that user
     * @author Andrew Rosenhaus
     */
    public ArrayList<MusicCollection> collectionSearch(String name, User user){
        ResultSet results;
        try{
            String query = "SELECT name, number_of_songs, total_time FROM music_collection WHERE LOWER(name) LIKE ? AND user_id = ? ORDER BY name ASC";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setString(1, name);
            pdst.setInt(2, user.getId());
            results = pdst.executeQuery();
            ArrayList<MusicCollection> returnList = new ArrayList<>();
            while (results.next())
                for (int j = 1; j <= 3; j++){
                    System.out.print(results.getString(j) + ", ");
                }
                returnList.add(new MusicCollection(results.getString(1), results.getInt(3), results.getInt(2)));
                System.out.println("");
                results.next();
                return returnList;
        }
        catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    /**
     * Updates name of collection
     * @param name
     * @param updatedName
     * @returns true if successful, false if error
     * @author Andrew Rosenhaus
     */
    public boolean modifyCollectionName(String name, String updatedName) {
        try {
            String query = "UPDATE music_collection FROM name = ? WHERE LOWER(name) LIKE ?";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setString(1, name);
            pdst.executeQuery();
            return true;
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Deletes collection
     * @param mc_id
     * @return true if successful, false if error
     * @author Andrew Rosenhaus
     */

    public boolean deleteCollection(Integer mc_id) {
        try {
            String query = "DELETE FROM music_collection WHERE mc_id = ?";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setInt(1, mc_id);
            pdst.executeQuery();
            return true;
        }
        catch (SQLException e) {
            System.out.println(e);
            return false;
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

    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        User testUser = test.login("MasterFaster", "RDA");
        System.out.println(testUser);
        test.collectionSearch("Synergistic object-orien", testUser);
        System.out.println(test.closeConnection());
    }
}