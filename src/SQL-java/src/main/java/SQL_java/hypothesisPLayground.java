package SQL_java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

public class hypothesisPLayground {
    private Connection connection;
    public static final String DBNAME = "p32001_05";

    public hypothesisPLayground(Connection connection) {
        this.connection = connection;
    }

    /**
     * This is the query being use for finding the top artist using the listening count,
     * and also finding the average song length of each artist
     * 
     * 
     * SELECT a.artID,
            a. artname,
            a.artistlistencount,
            b.avg_songlength
        FROM (SELECT artist.artist_id AS artid,
                    artist.name AS artname,
                    SUM(listencounttable.listencount) AS artistlistencount
                FROM artist
                LEFT JOIN created ON created.c_artist_id = artist.artist_id
                LEFT JOIN (SELECT song.song_id AS songID,song.title,
                                COUNT(listens_to.list_song_id) AS listencount
                            FROM song
                            LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id
                            GROUP BY song.song_id)
                AS listencounttable on songID = created.c_songid
                GROUP BY artist.artist_id, artist.name
                HAVING SUM(listencounttable.listencount) IS NOT NULL
                ORDER BY artistlistencount desc) as a JOIN
            (SELECT artist.artist_id AS artid,
                artist.name AS artname,
                round(AVG(listenlengthtable.songlength)/60, 2) AS avg_songlength
            FROM artist
            LEFT JOIN created ON created.c_artist_id = artist.artist_id
            LEFT JOIN (
                SELECT song.song_id AS songID,
                    song.length AS songlength
                FROM song
                LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id
                GROUP BY song.song_id
            ) AS listenlengthtable ON listenlengthtable.songID = created.c_songid
            GROUP BY artist.artist_id, artist.name
            HAVING AVG(listenlengthtable.songlength) IS NOT NULL
            ORDER BY avg_songlength DESC) as b on a.artid = b.artid
        ORDER BY a.artistlistencount DESC, b.avg_songlength DESC
        LIMIT 30;
     */
    public void getAverageLengthForTopArtist(){
        String query = "SELECT a.artID AS d, \r\n" + //
                        "       a. artname AS e,\r\n" + //
                        "       a.artistlistencount AS f,\r\n" + //
                        "       b.avg_songlength AS g\r\n" + //
                        "FROM (SELECT artist.artist_id AS artid,\r\n" + //
                        "             artist.name AS artname,\r\n" + //
                        "             SUM(listencounttable.listencount) AS artistlistencount\r\n" + //
                        "        FROM artist\r\n" + //
                        "        LEFT JOIN created ON created.c_artist_id = artist.artist_id\r\n" + //
                        "        LEFT JOIN (SELECT song.song_id AS songID,song.title,\r\n" + //
                        "                          COUNT(listens_to.list_song_id) AS listencount\r\n" + //
                        "                    FROM song\r\n" + //
                        "                    LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id\r\n" + //
                        "                    GROUP BY song.song_id)\r\n" + //
                        "        AS listencounttable on songID = created.c_songid\r\n" + //
                        "        GROUP BY artist.artist_id, artist.name\r\n" + //
                        "        HAVING SUM(listencounttable.listencount) IS NOT NULL\r\n" + //
                        "        ORDER BY artistlistencount desc) as a JOIN\r\n" + //
                        "    (SELECT artist.artist_id AS artid,\r\n" + //
                        "           artist.name AS artname,\r\n" + //
                        "           round(AVG(listenlengthtable.songlength)/60, 2) AS avg_songlength\r\n" + //
                        "    FROM artist\r\n" + //
                        "    LEFT JOIN created ON created.c_artist_id = artist.artist_id\r\n" + //
                        "    LEFT JOIN (\r\n" + //
                        "        SELECT song.song_id AS songID,\r\n" + //
                        "               song.length AS songlength\r\n" + //
                        "        FROM song\r\n" + //
                        "        LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id\r\n" + //
                        "        GROUP BY song.song_id\r\n" + //
                        "    ) AS listenlengthtable ON listenlengthtable.songID = created.c_songid\r\n" + //
                        "    GROUP BY artist.artist_id, artist.name\r\n" + //
                        "    HAVING AVG(listenlengthtable.songlength) IS NOT NULL\r\n" + //
                        "    ORDER BY avg_songlength DESC) as b on a.artid = b.artid\r\n" + //
                        "ORDER BY a.artistlistencount DESC, b.avg_songlength DESC\r\n" + //
                        "LIMIT 30;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet data = statement.executeQuery();

            System.out.println("Artist ID    Artist Name     Playcount       Average Song Length(mins)");
            
            while(data.next()){
                Integer artid = data.getInt("d");
                String artName = data.getString("e");
                Integer playCount = data.getInt("f");
                Float songLength = data.getFloat("g");
                System.out.println(artid + "          " + artName + "          " + playCount + "        " + songLength);
            }

        }   catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * This is the query to fetch the artist have the highest listen count
        SELECT artist.artist_id AS artid,
            artist.name AS artname,
            SUM(listencounttable.listencount) AS artistlistencount
        FROM artist
        LEFT JOIN created ON created.c_artist_id = artist.artist_id
        LEFT JOIN (SELECT song.song_id AS songID,song.title,
                        COUNT(listens_to.list_song_id) AS listencount
                    FROM song
                    LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id
                    GROUP BY song.song_id)
            AS listencounttable on songID = created.c_songid
        GROUP BY artist.artist_id, artist.name
        HAVING SUM(listencounttable.listencount) IS NOT NULL
        ORDER BY artistlistencount desc
        LIMIT 10
     */
    public void fetchTopArtist() {
        String query = "SELECT artist.artist_id AS artid,\r\n" + //
                        "            artist.name AS artname,\r\n" + //
                        "            SUM(listencounttable.listencount) AS artistlistencount\r\n" + //
                        "        FROM artist\r\n" + //
                        "        LEFT JOIN created ON created.c_artist_id = artist.artist_id\r\n" + //
                        "        LEFT JOIN (SELECT song.song_id AS songID,song.title,\r\n" + //
                        "                        COUNT(listens_to.list_song_id) AS listencount\r\n" + //
                        "                    FROM song\r\n" + //
                        "                    LEFT JOIN listens_to ON song.song_id = listens_to.list_song_id\r\n" + //
                        "                    GROUP BY song.song_id)\r\n" + //
                        "            AS listencounttable on songID = created.c_songid\r\n" + //
                        "        GROUP BY artist.artist_id, artist.name\r\n" + //
                        "        HAVING SUM(listencounttable.listencount) IS NOT NULL\r\n" + //
                        "        ORDER BY artistlistencount desc\r\n" + //
                        "        LIMIT 10";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();

            System.out.println("Artist ID    Artist Name     Playcount");
            while(results.next()){
                Integer artid = results.getInt("artid");
                String artName = results.getString("artname");
                Integer playCount = results.getInt("artistlistencount");

                System.out.println(artid + "          " + artName + "          " + playCount);
            }
        } catch (Exception e) {
            System.out.println(e);
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

        System.out.println("title      listen by follower");
            
            while(data.next()){
                String songName = data.getString("title");
                Integer followerListenCount = data.getInt("listenbyfollower");
                System.out.println(songName + "          " + followerListenCount);
            }
        
        return true;
        } catch (Exception e){
            System.out.println(e);
            return false;
        }
    }


    public static void main(String[] args) {
        DBFunction test = new DBFunction();
        Connection conn = test.getConnection();
        hypothesisPLayground check = new hypothesisPLayground(conn);
        // check.getAverageLengthForTopArtist();
        check.topSongFollowByUser(1);
        test.closeConnection();
    }
}
