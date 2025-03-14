package SQL_java;

/**
 * Please before asking why the connection is not establish, EDIT THIS PAGE 
 * to reflect the current password. I will add this file in .gitignore after first init so don't worry
 * Also REMEMBER to rename THIS FILE to abc.java so that the .gitignore actualy works
 * 
 */


public record CREDENTIAL() {
    public static final String DBLink = "jdbc:postgresql://127.0.0.1:5432/p32001_05";
    public static final String USERNAME = "AA";
    public static final String PASSWORD = "BB";
}