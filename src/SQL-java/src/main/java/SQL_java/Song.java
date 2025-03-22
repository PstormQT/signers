package SQL_java;

/**
 * A class to represent song data, specifically for searching
 * @author Antonio Bicknell <acb9430>
 */
public class Song {
    private int id;
    private String name;
    private String artist;
    private String album;
    private int length;
    private int listenCount;

    public Song(int id, String name, String artist, int length, int listenCount, String album){
        this.id =id;
        this.name = name;
        this.artist = artist;
        this.length = length;
        this.listenCount = listenCount;
        this.album = album;
    }

    public int getID(){ return id; }
    
    public String getName(){ return name; }

    public String getArtist(){ return artist; }

    public int getLength() { return length; }

    public int getListenCount() { return listenCount; }
    
    public String getAlbum(){ return album; }
    

    public void setListenCount(int newCount){
        listenCount = newCount;
    }


}
