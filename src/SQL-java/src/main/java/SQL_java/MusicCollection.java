package SQL_java;

public class MusicCollection {
    private String name;
    private int timeInMinutes;
    private int numberOfSongs;

    public MusicCollection (String name, int timeInMinutes,  int numberOfSongs){
        this.name = name;
        this.timeInMinutes = timeInMinutes;
        this.numberOfSongs = numberOfSongs;
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

    @Override
    public String toString(){
        return "Name: "+name+"\nTime (min): "+timeInMinutes+"\nNumber of Songs: "+numberOfSongs;
    }

}