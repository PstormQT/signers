package SQL_java;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/*
 * This is our class to access and interact with the Database. 
 * Any method/function that directly accesses the Database will and should be in here
 *      with VERY FEW EXCEPTIONS
 */
public class DBFunction{

    private Connection connection;

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
        } catch (Exception e) {
            System.err.println(e);
        }
    }


    public void testAccessData(String table_name){
        Statement statement;
        try {
            String query = "SELECT * FROM " + table_name ;
            statement = this.connection.createStatement();
            statement.executeQuery(query);
            System.err.println(statement);
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    /*
     * Retrieves an existing User's id, username, and password, and creates a User object
     * We were going to have a class for this, but anything that accesses the DB should be done in this class
     *  for ease of use and simplicity
     * @param username  The User's username
     * @param password  The User's password
     * @return          A User object if an existing user with the credentials is found. Null if otherwise
     */
    public User login(String username, String password){
        Statement statement;
        ResultSet results;
        try{
        String query = "SELECT  FROM users WHERE username='"+username+"'' AND password='"+password+"'";
        statement = this.connection.createStatement();
        results = statement.executeQuery(query);
        System.err.println(statement);
        if (results.next()){
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

    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        test.testAccessData("Users");
    }


}