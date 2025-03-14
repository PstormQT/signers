package SQL_java;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBFunction{

    private Connection connection;

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


    public void testCreateTable(String table_name){
        Statement statement;
        try {
            String query = "CREATE TABLE " + table_name + " (personid SERIAL PRIMARY KEY, lastname varchar(255));";


            statement = this.connection.createStatement();
            statement.executeUpdate(query);
            System.err.println("Table Created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        test.testCreateTable("HELLO");
    }


}