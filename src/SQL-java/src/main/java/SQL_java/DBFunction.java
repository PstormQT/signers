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
    private Session session = null;
    public static final String DBNAME = "p32001_05";

    public Connection getConnection(){
        return connection;
    }

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
        results = selectExec(pdst);
        closeConnection();
        if (results.next()){
            query = "UPDATE users SET last_login_date = ? WHERE username = ? AND password = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setDate(1, currentDate);
            pdstII.setString(2, username);
            pdstII.setString(3, password);
            updateExec(pdstII);
            closeConnection();         
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
            int rowsAffected = updateExec(pdst);
            closeConnection();
            System.out.println(rowsAffected);
            if(rowsAffected == 1){
                try{
                    String query2 = "SELECT user_id,username,password FROM users WHERE username = ? AND password = ?";
                    PreparedStatement pdstII = connection.prepareStatement(query2);
                    pdstII.setString(1, username);
                    pdstII.setString(2, password);
                    results = selectExec(pdstII);
                    closeConnection();
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
        // System.out.println(testUser);

        ArrayList<User> a = test.lookUpByEmail("ahowood1e@dagondesign.co");

        for(User user : a){
            System.out.println(user.getId());
            System.out.println(user.getUsername());
            System.out.println(user.getPassword());
        }



        System.out.println(test.closeConnection());
    }

}