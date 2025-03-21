public class MusicCollection {
    private String name;
    private int timeInMinutes;
    private int numberOfSongs;

    public MusicCollection (int id, String name, int timeInMinutes, private int numberOfSongs, int userId){
        this.name = name;
        this.timeInMinutes = timeInMinutes;
        this.numberOfSongs = numberOfSongs;
    }
    public String getName(){
        return name;
    }

    public String getTime(){
        return timeInMinutes;
    }

    public String getSongCount(){
        return numberOfSongs;
    }

    @Override
    public String toString(){
        return "Name: "+name+"\nTime (min): "+timeInMinutes+"\nNumber of Songs: "+numberOfSongs;
    }

}