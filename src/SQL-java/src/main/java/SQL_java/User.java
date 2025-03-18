package SQL_java;

/**
 * Do note the User Class only stores the identifying information for the User in question.
 * Any other data retrieval or updates should be done via SQL and the database
 * @author Brandon Yi
 */

public class User {
    private int id;
    private String username;
    private String password;

    public User (int id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

}
