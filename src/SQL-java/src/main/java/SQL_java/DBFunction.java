package SQL_java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;



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

    public Connection getConnection(){
        return connection;
    }

    public DBFunction() {
        this.currentDate = new java.sql.Date(utilDate.getTime());
        
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = abc.USERNAME; 
        String password = abc.PASSWORD; 
        String databaseName = DBNAME; 

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
        PreparedStatement statement;
        ResultSet results;
        try{

        String query = "SELECT * FROM users WHERE username=? AND password = cast(sha256(convert_to(CONCAT(?, sha256(convert_to(?, 'LATIN1') ) ),'LATIN1')) as varchar(256) )";
        //password = cast(sha256(convert_to((CONCAT(?, sha256(convert_to(?, 'LATIN1')))),'LATIN1')) as varchar(256))";
        
        statement = connection.prepareStatement(query);
        statement.setString(1, username);
        statement.setString(2, password);
        statement.setString(3, username);
        
        results = statement.executeQuery();
        if (results.next()){
            query = "UPDATE users SET last_login_date = ? WHERE username = ? AND password =cast(sha256(convert_to(CONCAT(?, sha256(convert_to(?, 'LATIN1') ) ),'LATIN1')) as varchar(256) )";
            PreparedStatement pdst = connection.prepareStatement(query);
            pdst.setDate(1, currentDate);
            pdst.setString(2, username);
            pdst.setString(3, password);
            pdst.setString(4, username);

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
            String query = "INSERT INTO users (password,creation_date,last_login_date,email,username,fname,lname) VALUES(cast(sha256(convert_to((CONCAT(?, sha256(convert_to(?, 'LATIN1')))),'LATIN1')) as varchar(256)),?,?,?,?,?,?)";

            PreparedStatement pdst = connection.prepareStatement(query);

            pdst.setString(1, password);
            pdst.setString(2, username);
            pdst.setDate(3, currentDate);
            pdst.setDate(4, currentDate);
            pdst.setString(5, email);
            pdst.setString(6, username);
            pdst.setString(7, fname);
            pdst.setString(8, lname);
            int rowsAffected = pdst.executeUpdate();
            //System.out.println(rowsAffected);
            if(rowsAffected == 1){
                try{
                    String query2 = "SELECT user_id,username,password FROM users WHERE username = ? AND password = cast(sha256(convert_to(CONCAT(?, sha256(convert_to(?, 'LATIN1') ) ),'LATIN1')) as varchar(256) )";
                    PreparedStatement pdstII = connection.prepareStatement(query2);
                    pdstII.setString(1, username);
                    pdstII.setString(2, password);
                    pdstII.setString(3, username);
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
     * @return List of all collections with that name from that user
     * @author Andrew Rosenhaus
     */
    public ArrayList<MusicCollection> collectionSearch(String name, Integer userId){
        ResultSet results = null;
        String query = "SELECT * FROM music_collection WHERE LOWER(name) LIKE LOWER(?) AND user_id = ? ORDER BY name ASC";
        ArrayList<MusicCollection> returnList = new ArrayList<>();
        try(PreparedStatement pdst = connection.prepareStatement(query);) {
            pdst.setString(1, "%" + name + "%");
            pdst.setInt(2, userId);
            results = pdst.executeQuery();
            System.out.println("Results:");
            while (results.next()){
                returnList.add(new MusicCollection(results.getString("name"), results.getInt("total_time"), results.getInt("number_of_songs"), results.getInt("mc_id"), results.getInt("user_id")));
            }
            for (int i = 0; i < returnList.size(); i++){
                System.out.println(returnList.get(i));
            }
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
                    return new MusicCollection(results.getString("name"), results.getInt("total_time"),
                    results.getInt("number_of_songs"), results.getInt("mc_id"), results.getInt("user_id"));
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
    public boolean deleteSongFromCollection(Integer song_id, Integer mc_id, Integer song_length){
        String query = "DELETE FROM collection_song WHERE song_id = ? AND mc_id = ?";
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setInt(1, song_id);
        pdst.setInt(2, mc_id);
        int rowsAffected = pdst.executeUpdate();
        if (rowsAffected == 1){
            query = "UPDATE music_collection SET total_time = total_time - ?, number_of_songs = number_of_songs - 1 WHERE mc_id = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(1, song_length);
            pdstII.setInt(2, mc_id);
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
    public boolean addSongToCollection(Integer song_id, Integer mc_id, Integer total_time){
        String query = "INSERT INTO collection_song (song_id, mc_id) VALUES (?,?)";
        try(PreparedStatement pdst = connection.prepareStatement(query);){
        pdst.setInt(1, song_id);
        pdst.setInt(2, mc_id);
        int rowsAffected = pdst.executeUpdate();
        if (rowsAffected == 1){
            query = "UPDATE music_collection SET total_time = ? WHERE mc_id = ?";
            PreparedStatement pdstII = connection.prepareStatement(query);
            pdstII.setInt(1, total_time);
            pdstII.setInt(2, mc_id);
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
    public boolean modifyCollectionName(Integer MCId, String updatedName) {
        String query = "UPDATE music_collection SET name = ? WHERE mc_id = ?";
        try(PreparedStatement pdst = connection.prepareStatement(query)) {
            pdst.setString(1, updatedName);
            pdst.setInt(2, MCId);
            if(pdst.executeUpdate() == 1){
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
     * Deletes collection
     * @param mc_id
     * @return true if successful, false if error
     * @author Andrew Rosenhaus
     */

    public boolean deleteCollection(Integer mc_id) {
        ResultSet results = null;

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
     * Gives recomendations to the user for what song to listen to based on their most listened to genre.
     * @return true or false depending on if user listening data was found or not
     * @author Katie Richardson
     */
    public boolean getRecommendations(int list_user_id){
        ResultSet results = null;
        // Top listened to genre from user
        String query1 = "SELECT genre.genre_name AS genrename, " +
            "COUNT(*) AS totalListenCount " +
            "FROM song " +
            "JOIN song_genre ON song.song_id = song_genre.song_id " +
            "JOIN genre ON song_genre.genre_id = genre.genre_id " +
            "JOIN listens_to ON song.song_id = listens_to.list_song_id " +
            "WHERE listens_to.list_user_id = ? " +
            "GROUP BY genre.genre_name " +
            "ORDER BY totalListenCount DESC LIMIT 1";
        // Recommend top 10 songs of that genre
        String query2 = "SELECT song.song_id, song.title, COUNT(listens_to.list_user_id) AS total_listens " +
            "FROM song JOIN song_genre ON song.song_id = song_genre.song_id " +
            "LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id " +
            "JOIN genre on song_genre.genre_id = genre.genre_id " +
            "WHERE genre.genre_name = ? " +
            "GROUP BY song.song_id, song.title " +
            "ORDER BY total_listens DESC LIMIT 10";
        try(PreparedStatement pdst = connection.prepareStatement(query1);){
            pdst.setInt(1,list_user_id);
            results = pdst.executeQuery();
            if(results.next()) {
                String genre = results.getString("genrename");
                PreparedStatement pdstII = connection.prepareStatement(query2);
                pdstII.setString(1, genre);
                results = pdstII.executeQuery();
                ArrayList<String> songs = new ArrayList<>();
                while(results.next()) {
                    songs.add(results.getString("title"));
                }
                for(int i = 1; i <= songs.size(); i++){
                    System.out.println(i + ": " + songs.get(i-1));
                }
                return true;
            }
            else {
                System.out.println("User has no listening history");
            }
            return false;
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



    public static final String DEFAULT_SORT = "s.title, artist_names";
    /**
     * 
     * @param containedText The text we are searching to be in the title, artists, albums, or genres
     * @param orderBy, the Ordering we are going by, either DEFAULT_SORT, title, artist_names, genre_names, or release_date
     * @param ascending, true if asc order, desc otherwise
     * @return An arraylist of all songs with containedText as part of it's title, artist, albums, or genres
     * @author Antonio Bicknell <acb930>
     */
    public ArrayList<Song> searchSongs(String containedText, String orderBy, boolean ascending){
        if(ascending){
        orderBy += " ASC ";
        }
        else{
            orderBy += " DESC ";
        }
/**THE QUERY FOR THIS FUNCTION IS AS FOLLOWS:
        SELECT s.song_id, s.title, s.length, s.playcount, string_agg(a.name, ', ' ORDER BY a.name) AS artist_names,
                                            string_agg(g.genre_name, ', 'ORDER BY g.genre_name) AS genre_names,
                                            string_agg(alb.name, ', ' ORDER BY alb.name) AS album_names
        FROM song s INNER JOIN artist a
            ON EXISTS(SELECT * FROM created WHERE c_artist_id = a.artist_id AND c_songid = s.song_id)
        INNER JOIN genre g
            ON EXISTS(SELECT * FROM song_genre WHERE song_genre.song_id = s.song_id AND song_genre.genre_id = g.genre_id)
        INNER JOIN album alb
            ON EXISTS(SELECT * FROM album_song WHERE album_song.col_album_id = alb.album_id     AND album_song.col_song_id = s.song_id)
        WHERE TRUE
        GROUP BY s.song_id, s.title
        HAVING
            s.title LIKE '%bob%'
            OR string_agg(a.name, ', ') LIKE'%bob%'
            OR string_agg(g.genre_name, ', ') LIKE '%bob%'
            OR string_agg(alb.name, ', ') LIKE '%bob%'
        ORDER BY title, artist_names

         */

        String query = "SELECT s.song_id, s.title, s.length, s.playcount, STRING_AGG(a.name, ', ' ORDER BY a.name) AS artist_names, STRING_AGG(g.genre_name, ', ' ORDER BY g.genre_name) as genre_names, " + 
        "STRING_AGG(alb.name, ', ' ORDER BY alb.name) AS album_names " +
        "FROM song s INNER JOIN artist a ON EXISTS(SELECT * FROM created WHERE c_artist_id = a.artist_id AND c_songid = s.song_id) " + 
        "INNER JOIN genre g ON EXISTS(SELECT * FROM song_genre WHERE song_genre.song_id = s.song_id AND song_genre.genre_id = g.genre_id) " + 
        "INNER JOIN album alb ON EXISTS(SELECT * FROM album_song WHERE album_song.col_album_id = alb.album_id AND album_song.col_song_id = s.song_id) " +
        "WHERE TRUE GROUP BY s.song_id, s.title HAVING s.title LIKE ? OR STRING_AGG(a.name, ', ') LIKE ? " + 
        "OR STRING_AGG(g.genre_name, ', ') LIKE ? OR STRING_AGG(alb.name, ', ') LIKE ?" +
        "ORDER BY ?" ;
        try(PreparedStatement preparedST = connection.prepareStatement(query)) {
            preparedST.setString(1, "%" + containedText + "%");
            preparedST.setString(2, "%" + containedText + "%");
            preparedST.setString(3, "%" + containedText + "%"); 
            preparedST.setString(4, "%" + containedText + "%"); 
            preparedST.setString(5, orderBy);

            ResultSet results = preparedST.executeQuery();
            ArrayList<Song> songs = new ArrayList<Song>();
            while(results.next()){
                songs.add(new Song( results.getInt("song_id"), 
                                    results.getString("title"),
                                    results.getString("artist_names"),
                                    results.getInt("length"),
                                    results.getInt("playcount"),  
                                    results.getString("album_names") ) );
            }
            //Now that all added, good to return
            //Printing the information is the responsibility of the TUI

            return songs;

        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }



        

    }
    

    /**
     * Listens to a specific song with a particular ID
     * And also updates the listen count of the song
     * @param songID The ID of the song we are listening to 
     * @param user The user that is listening to the song
     * @return Whether or not a row was changed, as a boolean
     * @author Antonio Bicknell <acb9430>
     */
    public boolean listenToSong(int songID, User user){
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



        return rowsAffected > 0;
        }
        catch(SQLException e){
            System.err.println(e);
            return false;
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


    public boolean topSongFollowByUser(int userid){
        try{
            String query = "SELECT song.title,\r\n" + //
                        "        listenbyfollower\r\n" + //
                        "FROM (SELECT listens_to.list_user_id AS userid,\r\n" + //
                        "               listens_to.list_song_id AS songid,\r\n" + //
                        "                COUNT(list_user_id) as listenbyfollower\r\n" + //
                        "        from (SELECT following.following_id\r\n" + //
                        "                FROM following\r\n" + //
                        "                WHERE following.user_id = ?) AS followertable\r\n" + //
                        "        LEFT JOIN listens_to on following_id = list_user_id\r\n" + //
                        "        GROUP BY list_song_id, list_user_id) AS followusertopsong\r\n" + //
                        "LEFT JOIN song on followusertopsong.songid = song.song_id\r\n" + //
                        "ORDER BY listenbyfollower DESC\r\n" + //
                        "LIMIT 50";
        PreparedStatement statement = this.connection.prepareStatement(query);
        statement.setInt(1, userid);
        
        ResultSet data = statement.executeQuery();

        System.out.println("title      Listens by follower");
            
            while(data.next()){
                String songName = data.getString("title");
                Integer followerListenCount = data.getInt("listenbyfollower");
                System.out.println(songName + "\t\t" + followerListenCount);
            }
        
        return true;
        } catch (Exception e){
            System.out.println(e);
            return false;
        }
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
        //System.out.println(rowsAffected);
        return rowsAffected > 0;

    } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Calculates the most listened to genres in the current calendar month i.e 2025-4-1 -> 2025-4-30, inclusive
     * @return Each Genre's name, as a string, in an ArrayList
     * @author Antonio Bicknell <acb9430>
     */
    public ArrayList<String> top5MonthGenre(){
        ResultSet result = null;
        PreparedStatement pdst = null;
        ArrayList<String> output = new ArrayList<String>();
        String query =  "SELECT g.genre_name " +  //, COUNT(l.list_song_id) optional if we want listen count
                        "FROM genre as g LEFT JOIN song_genre AS sg ON(g.genre_id=sg.genre_id) " + 
                        "LEFT JOIN listens_to AS l ON(l.list_song_id = sg.song_id)  " + 
                        "WHERE date_trunc('month',l.date_time_listened) = date_trunc('month', NOW()) " + 
                        "GROUP BY g.genre_id ORDER BY COUNT(l.list_song_id) DESC LIMIT 5";
        try{
           pdst = this.connection.prepareStatement(query);
           result = pdst.executeQuery(); 
           while(result.next()){
                output.add(result.getString(1));
           }
           return output;
        }
        catch(Exception e){
            System.out.println(e);
            return null;
        }
    }


    /**
     * @return an Arraylist containing the top 50 songs by total listens from the past 30 days
     * Result can be less than 50 in size, as many songs have 0 listens in the past 30 days
     */
    public ArrayList<String> top50month(){
        ResultSet result = null;
        PreparedStatement pdst1 = null;
        ArrayList<String> output = new ArrayList<>();
        String query = "SELECT title, listCount.listenCount FROM song LEFT JOIN " +
        "(SELECT list_song_id, COUNT(list_song_id) AS listenCount " +
        "FROM listens_to " +
        "WHERE date_time_listened >= (NOW() - INTERVAL '30 days') " +
        "GROUP BY list_song_id " +
        "ORDER BY COUNT(list_song_id) desc) listCount " +
        "ON song.song_id = listCount.list_song_id " +
        "WHERE listCount.listenCount IS NOT NULL " +
        "ORDER BY listCount.listenCount desc " +
        "LIMIT 50";
        try{
            pdst1 = this.connection.prepareStatement(query);
            result = pdst1.executeQuery();
            while(result.next()){
                output.add(result.getString(1));
            }
            return output;
        }
        catch(Exception e){
            System.out.println(e);
            return null;
        }

    }


    /**
     * Pulls up the User Profile
     * @param user the ID of the user being checked
     * Author: Andrew Rosenhaus
     */
    public void checkUserProfile(int user){
        PreparedStatement query = null;
        ResultSet rs = null;
        try{
            String getName = "SELECT username FROM users WHERE user_id = ?";
            query = this.connection.prepareStatement(getName);
            query.setInt(1, user);
            rs = query.executeQuery();
            rs.next();
            System.out.println("User: " + rs.getString(1));
            checkUserTopArtists(user);
            checkFollowerCount(user);
            checkFollowingCount(user);
            checkCollectionCount(user);
        }
        catch(SQLException e){
            System.out.println(e);
        }      
    }
    
    /**
     * Pulls up the User's top artists
     * @param user the ID of the user being checked
     * Author: Andrew Rosenhaus
     */
    public void checkUserTopArtists(int user){
        PreparedStatement query = null;
        ResultSet rs = null;
        try{
            String getArtists = "SELECT name FROM artist INNER JOIN (SELECT c_artist_id FROM created INNER JOIN (SELECT list_song_id FROM listens_to WHERE list_user_id = ?) AS songs ON created.c_songid = songs.list_song_id GROUP BY c_artist_id ORDER BY COUNT(*) DESC LIMIT 10) AS top_artists ON artist.artist_id = top_artists.c_artist_id";
            query = this.connection.prepareStatement(getArtists);
            query.setInt(1,user);
            rs = query.executeQuery();
            System.out.println("Top 10 Artists:");
            int interval = 1;
            while(rs.next()){
                System.out.println(interval + ": " + rs.getString(1));
                interval++;
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }
    /**
     * Prints the number of followers a user has
     * @param user the ID of the user being checked
     * Author: Andrew Rosenhaus
     */
    public void checkFollowerCount(int user){
        PreparedStatement query = null;
        ResultSet rs = null;
        try{
            String getFollowerCount = "SELECT COUNT(*) FROM following WHERE following_id = ?";
            query = this.connection.prepareStatement(getFollowerCount);
            query.setInt(1,user);
            rs = query.executeQuery();
            while(rs.next()){
                System.out.println("Followers: " + rs.getString(1));
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }

    /**
     * Prints the number of users a user is following
     * @param user the ID of the user being checked
     * Author: Andrew Rosenhaus
     */
    public void checkFollowingCount(int user){
        PreparedStatement query = null;
        ResultSet rs = null;
        try{
            String getFollowerCount = "SELECT COUNT(*) FROM following WHERE user_id = ?";
            query = this.connection.prepareStatement(getFollowerCount);
            query.setInt(1,user);
            rs = query.executeQuery();
            while(rs.next()){
                System.out.println("Following: " + rs.getString(1));
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }
    /**
     * Checks the number of collections a user has
     * @param user the ID of the user being checked
     * Author: Andrew Rosenhaus
     */
    public void checkCollectionCount(int user){
        PreparedStatement query = null;
        ResultSet rs = null;
        try{
            String getFollowerCount = "SELECT COUNT(*) FROM music_collection WHERE user_id = ?";
            query = this.connection.prepareStatement(getFollowerCount);
            query.setInt(1,user);
            rs = query.executeQuery();
            while(rs.next()){
                System.out.println("Collections: " + rs.getString(1));
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }


    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        Scanner scanner = new Scanner(System.in);
        User currentUser = null;
        boolean sentinal = true;
        String input;
        System.out.println("Welcome to the Music Database!");
        boolean loggingIn = true;
        while (loggingIn){
            System.out.println("Would you like to login or create a new user?");
            input = scanner.nextLine();
            if(input.equals("login")){
                System.out.println("enter username:");
                String username = scanner.nextLine();
                System.out.println("enter password:");
                String password = scanner.nextLine();
                currentUser = test.login(username, password);
                if (currentUser != null){
                    System.out.println("Welcome, " + currentUser.getUsername());
                    loggingIn = false;
                }
                else{
                    System.out.println("User not found");
                }
            }
            if(input.equals("new user")){
                System.out.println("enter username:");
                String username = scanner.nextLine();
                System.out.println("enter password:");
                String password = scanner.nextLine();
                System.out.println("enter email:");
                String email = scanner.nextLine();
                System.out.println("enter first name:");
                String first = scanner.nextLine();
                System.out.println("enter last name:");
                String last = scanner.nextLine();
                currentUser = test.createUser(username, password, first, last, email);
                if(currentUser != null){
                    System.out.println("Welcome, " + currentUser.getUsername());
                    loggingIn = false;
                }
                else{
                    System.out.println("User creation failed");
                }
            }
        }


        while(sentinal){
            System.out.println("Collection    Song     User    Recommendations     Quit");
            input = scanner.nextLine();
            input.toLowerCase();
            if (input.equals("quit")){
                sentinal = false;
                scanner.close();
                test.closeConnection();
            }
            if (input.equals("collection")){
                System.out.println("Create    Add Song   Delete Song    Change Name    Delete Collection    Search Collections    Listen");
                String input2 = scanner.nextLine();
                input2 = input2.toLowerCase();
                if(input2.equals("create")){
                    System.out.println("Input the desired name.");
                    String name = scanner.nextLine();
                    test.createCollection(name, 0, 0, currentUser.getId());
                }
                if(input2.equals("add song")){
                    System.out.println("Input the ID of the song you want to add.");
                    Integer songId = scanner.nextInt();
                    System.out.println("Input the ID of the collection you want to modify.");
                    Integer MCID = scanner.nextInt();
                    System.out.println("Input the length of the song you want to add.");
                    Integer songLength = scanner.nextInt();
                    test.addSongToCollection(songId, MCID, songLength);
                }
                if(input2.equals("delete song")){
                    System.out.println("Input the ID of the song you want to delete.");
                    Integer songId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Input the ID of the collection you want to modify.");
                    Integer MCID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Input the length of the song you want to delete.");
                    Integer songLength = scanner.nextInt();
                    scanner.nextLine();
                    test.deleteSongFromCollection(songId, MCID, songLength);
                }
                if(input2.equals("change name")){
                    System.out.println("Input the ID of the collection you want to change the name of.");
                    Integer mc_id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Input the desired name.");
                    String name = scanner.nextLine().toLowerCase();
                    test.modifyCollectionName(mc_id, name);
                }
                if(input2.equals("delete")){
                    System.out.println("Input the ID of the collection you want to delete.");
                    Integer mc_id = scanner.nextInt();
                    scanner.nextLine();
                    test.deleteCollection(mc_id);
                }
                if(input2.equals("search")){
                    System.out.println("Input the desired name to search.");
                    String name = scanner.nextLine().toLowerCase();
                    test.collectionSearch(name, currentUser.getId());

                }
                if(input2.equals("listen")){
                    System.out.println("Input the ID of the collection to listen to");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    test.listenToCollection(id, currentUser);
                }
            }
            if (input.equals("song")){
                System.out.println("listen      search         ranking");
                String songInput = scanner.nextLine();
                if (songInput.equals("listen")){
                    System.out.println("Please enter song ID");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    test.listenToSong(id, currentUser);
                    System.out.println("listened!");
                }
                if (songInput.equals("search")){
                    System.out.println("Please enter search term");
                    songInput = scanner.nextLine();
                    System.out.println("Sort by: default, title, artist_names, genre_names, or release_date?");
                    String sortBy = scanner.nextLine();
                    if (sortBy.equals("default")) {
                        sortBy = DEFAULT_SORT;
                    }
                    System.out.println("ascending or descending?");
                    String asc = scanner.nextLine();
                    if(asc.equals("ascending")){
                        ArrayList<Song> results = test.searchSongs(songInput, sortBy, true);
                        for (Song i : results){
                            System.out.println(i);
                        }
                    }
                    if(asc.equals("descending")){
                        ArrayList<Song> results = test.searchSongs(songInput, sortBy, false);
                        for (Song i : results){
                            System.out.println(i);
                        }
                    }
                    
                }

                if (songInput.equals("ranking")){
                    System.out.println("Recent popular songs\tFriends' popular songs\tGenre montly top");
                    String rankingInput = scanner.nextLine().toLowerCase();
                    //TOP 50 OF THE PAST 30 DAYS
                    if(rankingInput.equals("recent") || rankingInput.equals("recent popular") || rankingInput.equals("recent popular songs")){
                        /* 
                        System.out.println("top 50");
                        songInput = scanner.nextLine();
                        if (songInput.equals("top 50")) {
                        */
                            System.out.println("Here is the top 50 for this month");
                            ArrayList<String> top = test.top50month();
                            for(String title : top){
                            System.out.println(title);
                            }
                    }
                    //TOP 5 GENRE OF MONTH
                    if(rankingInput.equals("genre") || rankingInput.equals("genre monthly") || rankingInput.equals("genre monthly top")){
                        System.out.println("Top 5 Genres of the month");
                        ArrayList<String> genres = test.top5MonthGenre();
                        for(int i = 0; i<genres.size(); i++){
                            System.out.println((i+1) + ".\t" + genres.get(i));
                        }
                    }
                    //TOP SONGS BY FOLLOWED USERS
                    if(rankingInput.equals("friends") || rankingInput.equals("friends popular") || rankingInput.equals("friends' popular songs"))
                    {
                        System.out.println("Top 50 Songs Listened To By Followed Users");
                        test.topSongFollowByUser(currentUser.getId());
                    }
                }

            }

            
            if (input.equals("user")){
                System.out.println("Follow      Unfollow      View");
                String userInput = scanner.nextLine();
                if (userInput.equals("follow")) {
                    System.out.println("ID of user to follow");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    if(test.userFollowing(currentUser.getId(), id)){
                        System.out.println("Successfully Followed!");
                    }
                }
                if(userInput.equals("unfollow")){
                    System.out.println("ID of user to unfollow");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    if(test.userUnFollowing(currentUser.getId(), id)){
                        System.out.println("Successfully unfollowed!");
                    }
                }
                if(userInput.equals("view")){
                    System.out.println("ID of user to view");
                    int id = scanner.nextInt();
                    test.checkUserProfile(id);
                }
            }
            if (input.equals("recommendations")){
                System.out.println("Self      Other");
                String userInput = scanner.nextLine();
                if(userInput.equals("self")){
                    test.getRecommendations(currentUser.getId());
                }
                if(userInput.equals("other")){
                    System.out.println("ID of user to see recommendations:");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    test.getRecommendations(id);
                }
            }
        }
    }
}