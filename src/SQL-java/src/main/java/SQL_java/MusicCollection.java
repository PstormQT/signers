package SQL_java;

public class MusicCollection {
    private String name;
    private int timeInMinutes;
    private int numberOfSongs;
    private int mcId;
    private int userId;

    public MusicCollection (String name, int timeInMinutes,  int numberOfSongs, int mcId, int userId){
        this.name = name;
        this.timeInMinutes = timeInMinutes;
        this.numberOfSongs = numberOfSongs;
        this.mcId = mcId;
        this.userId = userId;
    }
    public String getName(){
        return name;
    }

    public int getTime(){
        return timeInMinutes;
    }

    public int getSongCount(){
        return numberOfSongs;
    }

    public int getMCId(){
        return mcId;
    }
    public int getUId(){
        return userId;
    }
    @Override
    public String toString(){
        return "Name: "+name+"\nTime (min): "+timeInMinutes+"\nNumber of Songs: "+numberOfSongs;
    }

}