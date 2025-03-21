package SQL_java;

public class Song {
    private int id;
    private String name;
    private String artist;
    private int length;
    private int listenCount;

    public Song(int id, String name, String artist, int length, int listenCount){
        this.id =id;
        this.name = name;
        this.artist = artist;
        this.length = length;
        this.listenCount = listenCount;
    }

    public int getID(){ return id; }
    
    public String getName(){ return name; }

    public String getArtist(){ return artist; }

    public int getLength() { return length; }

    public int getListenCount() { return listenCount; }

    public void setListenCount(int newCount){
        listenCount = newCount;
    }


}
