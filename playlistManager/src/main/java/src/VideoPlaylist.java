/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 *
 * @author Thema
 */
public final class VideoPlaylist {
    // The Variables
    
    // The Strings
    private String playlistPath;
    private String playlistID;
    private String thumbnailURL;
    private String playlistName;
    private String filteredPlaylistName;
    private String description;
    private String channel;
    
    // The Longs
    private int thumbnailHeight;
    private int thumbnailWidth;
    private int listSize;
    private int loopCount;
    
    private double speed;
    
    // The booleans
    private boolean overrideSettings;
    private boolean isLooping;
    private boolean isSpeedChanged;
    private boolean isSaved;
    
    // The lists
    private ArrayList<PlaylistVideo> videos; 
    private ArrayList<String> tags;
    
    // The misclanneous variables
    private File playlistFile;
    private PrintWriter playlistFileWriter;
    private Scanner playlistFileReader;
    
    VideoPlaylist(){
        playlistID = "";
        thumbnailURL = "";
        playlistName = "";
        description = "";
        channel = "";
        thumbnailHeight = 0;
        thumbnailWidth = 0;
        listSize = 0;
        loopCount = 0;
        speed = 1;
        overrideSettings = false;
        isLooping = false;
        isSpeedChanged = false;
        isSaved = false;
        videos = new ArrayList<>();
    }
    
    VideoPlaylist(String path, String pn) throws IOException{
        
        filteredPlaylistName = pn.replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>?]|[.]{2,}$|[.]$", "").trim();
        playlistPath = path;
        playlistFile = new File(playlistPath, "playlist.txt"); 
        
        playlistFileReader = new Scanner(playlistFile);
        if(playlistFile.exists() && playlistFileReader.hasNext()){
            
            playlistFileReader.skip("Playlist ID: ");
            playlistID = playlistFileReader.nextLine();
            
            playlistFileReader.skip("Playlist Name: ");
            playlistName = playlistFileReader.nextLine();
            
            playlistFileReader.skip("Description: ");
            while(!playlistFileReader.hasNext("Channel:")){
                description += playlistFileReader.nextLine() + "\n";
            }
            if(description == null){
                description = "";
                playlistFileReader.nextLine();
            }
            
            playlistFileReader.skip("Channel: ");
            channel = playlistFileReader.nextLine();
            
            playlistFileReader.skip("Thumbnail URL: ");
            thumbnailURL = playlistFileReader.nextLine();
            
            playlistFileReader.skip("Thumbnail Height: ");
            thumbnailHeight = playlistFileReader.nextInt();
            playlistFileReader.nextLine();
            
            playlistFileReader.skip("Thumbnail Width: ");
            thumbnailWidth = playlistFileReader.nextInt();
            playlistFileReader.nextLine();
            
            playlistFileReader.skip("Size: ");
            listSize = playlistFileReader.nextInt();
            playlistFileReader.nextLine();
            
            playlistFileReader.skip("Loop Count: ");
            loopCount = playlistFileReader.nextInt();
            playlistFileReader.nextLine();

            playlistFileReader.skip("Speed: ");
            speed = playlistFileReader.nextDouble();
            playlistFileReader.nextLine();

            playlistFileReader.skip("Override Video Settings: ");
            overrideSettings = playlistFileReader.nextBoolean();
            playlistFileReader.nextLine();
            
            playlistFileReader.skip("Are Videos Looped: ");
            isLooping = playlistFileReader.nextBoolean();
            playlistFileReader.nextLine();

            playlistFileReader.skip("Are Video Speeds Changed: ");
            isSpeedChanged = playlistFileReader.nextBoolean();
            playlistFileReader.nextLine();

            playlistFileReader.skip("Are Videos Saved: ");
            isSaved = playlistFileReader.nextBoolean();
            playlistFileReader.nextLine();
                
            playlistFileReader.skip("Tags: ");
            tags = new ArrayList<>();
            setTags(playlistFileReader.nextLine());
            
            videos = new ArrayList<>();
            
            playlistFileReader.close();
        }
    }
    
    // The Normal Constructor
    VideoPlaylist(String path, String id, String pn, String d, String c, String tu, int tw, int th) throws IOException{
        
        filteredPlaylistName = pn.replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>?]|[.]{2,}$|[.]$", "").trim();
        playlistPath = path;
        
        playlistFile = new File(playlistPath, "playlist.txt");
        playlistFile.createNewFile();
        
        
        playlistFileWriter = new PrintWriter(playlistFile, "UTF-8");
        playlistID = id;
        playlistFileWriter.println("Playlist ID: " + id);

        playlistName = pn;
        playlistFileWriter.println("Playlist Name: " + pn);

        description = d;
        playlistFileWriter.println("Description: " + d);

        channel = c;
        playlistFileWriter.println("Channel: " + c);

        thumbnailURL = tu;
        playlistFileWriter.println("Thumbnail URL: " + tu);

        thumbnailHeight = th;
        playlistFileWriter.println("Thumbnail Height: " + th);

        thumbnailWidth = tw;
        playlistFileWriter.println("Thumbnail Width: " + tw);

        listSize = 0;
        playlistFileWriter.println("Size: " + listSize);
        
        loopCount = 0;
        playlistFileWriter.println("Loop Count: " + loopCount);
        
        speed = 1;
        playlistFileWriter.println("Speed: " + speed);
        
        overrideSettings = false;
        playlistFileWriter.println("Override Video Settings: " + overrideSettings);
        
        isLooping = false;
        playlistFileWriter.println("Are Videos Looped: " + isLooping);
        
        isSpeedChanged = false;
        playlistFileWriter.println("Are Video Speeds Changed: " + isSpeedChanged);
        
        isSaved = false;
        playlistFileWriter.println("Are Videos Saved: " + isSaved);
        
        tags = new ArrayList<>();
        playlistFileWriter.println("Tags: None");
        
        videos = new ArrayList<>();
        
        playlistFileWriter.close();
        
    }
    
    private void replaceText(String oldText, String newText) throws FileNotFoundException, UnsupportedEncodingException, UnsupportedEncodingException{
        
        if(!oldText.equals(newText)){
            playlistFileReader = new Scanner(playlistFile);
        
            String fileText = "";

            while(playlistFileReader.hasNext()){
                fileText += playlistFileReader.nextLine() + "\n";
            }

            fileText = fileText.replace(oldText, newText);
            playlistFileWriter = new PrintWriter(playlistFile, "UTF-8");
            playlistFileWriter.write(fileText);

            playlistFileReader.close();
            playlistFileWriter.close();
        }
    }
    
    // The Getters
    
    public String getPlaylistPath(){
        return playlistPath;
    }
    
    // Returns the Playlist ID
    public String getPlaylistID() {
        return playlistID;
    }
    
    // Returns the Thumbnail URL
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    // Returns the Playlist Name
    public String getPlaylistName() {
        return playlistName;
    }
    
    public String getFilteredName() {
        return filteredPlaylistName;
    }

    // Returns the Playlist Description
    public String getDescription() {
        return description;
    }

    // Returns the Channel Name
    public String getChannel() {
        return channel;
    }

    // Returns the Thumbnail Height
    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    // Returns the Thumbnail Width
    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public PlaylistVideo getVideo(int v){
        if(v >= getSize()){
            return videos.get(videos.size()-1);
        } else if (v < 0){
            return videos.get(0);
        } else {
            return videos.get(v);
        }
    }
    
    public int indexOf(PlaylistVideo v){
        return getPlaylist().indexOf(v);
    }
    
    public ArrayList<PlaylistVideo> getPlaylist(){
        return videos;
    }
    
    public int getSize(){
        return listSize;
    }
    
     // Returns the Video's loop count
    public int getLoopCount(){
        return loopCount;
    }
    
    public double getSpeed(){
        return speed;
    }
    
    public boolean getOverrideSettings(){
        return overrideSettings;
    }
    
    public boolean getIsLooping(){
        return isLooping;
    }
    
    public boolean getIsSpeedChanged(){
        return isSpeedChanged;
    }
    
    public boolean getIsSaved(){
        return isSaved;
    }
    
    public String getTags(){
        return tags.toString().replace("[", "").replace("]", "");
    }
    
    // The Setters
    // Sets the Playlist's ID
    public void setPlaylistID(String id) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Playlist ID: " + playlistID, "Playlist ID: " + id);
        playlistID = id;
    }
    
    // Sets the Playlist's Name
    public void setPlaylistName(String pn) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Playlist Name: " + playlistName, "Playlist Name: " + pn);
        playlistName = pn;
    }

    // Sets the Video's Description
    public void setDescription(String d) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Description: " + description, "Description: " + d);
        description = d;
    }

    // Sets the Video's Channel Name
    public void setChannel(String c) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Channel: " + channel, "Channel: " + c);
        channel = c;
    }
    
    // Sets the Thumbnail's URL
    public void setThumbnailURL(String tu) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail URL: " + thumbnailURL, "Thumbnail URL: " + tu);
        thumbnailURL = tu;
    }

    // Sets the Thumbnail's Height
    public void setThumbnailHeight(int th) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail Height: " + thumbnailHeight, "Thumbnail Height: " + th);
        thumbnailHeight = th;
    }

    // Sets the Thumbnail's Width
    public void setThumbnailWidth(int tw) throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail Width: " + thumbnailWidth, "Thumbnail Width: " + tw);
        thumbnailWidth = tw;
    }
    
    public void setSize(int s) throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Size: " + listSize, "Size: " + s);
        listSize = s;
    }
    
    // Sets the Video's tags
    public void setTags(String newTags) throws FileNotFoundException, UnsupportedEncodingException{
        
        // Create a new ArrayList 
        ArrayList<String> uniqueTags = new ArrayList<>(); 
  
        for (String tag : newTags.split(",")) {
            if (!uniqueTags.contains(tag.trim())) { 
                uniqueTags.add(tag.trim()); 
            } 
        } 
        
        if(uniqueTags.isEmpty()){
            uniqueTags.add("None");
        }
        if(uniqueTags.size() > 1 && uniqueTags.contains("None")){
            while(uniqueTags.contains("None")){
                uniqueTags.remove("None");
            }
        }
        
        if(getTags().isEmpty()){
            replaceText("Tags: None", "Tags: " + uniqueTags.toString().replace("[", "").replace("]", ""));
        } else if(!tags.containsAll(uniqueTags)){
            replaceText("Tags: " + getTags(), "Tags: " + uniqueTags.toString().replace("[", "").replace("]", ""));
        }
        
        // Removes the ['s and ]'s, and splits the string into a list by the commas
        tags.clear();
        tags.addAll(uniqueTags);
    }
    
    // Sets the Video's Loop Count
    public void setLoopCount(int lc)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Loop Count: " + loopCount, "Loop Count: " + lc);
        loopCount = lc;
    }
    
    public void setSpeed(double s)  throws FileNotFoundException, UnsupportedEncodingException {
        double tempValue = s;
        if(tempValue > 3){
            tempValue = 3;
        } else if (tempValue < .5){
            tempValue = .5;
        }
        replaceText("Speed: " + speed, "Speed: " + tempValue);
        speed = tempValue;
    }
    
    public void setOverrideSettings(boolean o)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Override Video Settings: " + overrideSettings, "Override Video Settings: " + o);
        overrideSettings = o;
    }
    
    public void setIsLooping(boolean l)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Are Videos Saved: " + isLooping, "Are Videos Saved: " + l);
        isLooping = l;
    }
    
    public void setIsSpeedChanged(boolean sc)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Are Video Speeds Changed: " + isSpeedChanged, "Are Video Speeds Changed: " + sc);
        isSpeedChanged = sc;
    }
    
    public void setIsSaved(boolean s)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Are Videos Saved: " + isSaved, "Are Videos Saved: " + s);
        isSaved = s;
    }
    
    // Sets the Video's tags
    public void addTags(String newTags) throws FileNotFoundException, UnsupportedEncodingException{
        
        ArrayList<String> uniqueTags = new ArrayList<>();
        uniqueTags.addAll(tags);
        
        for(String tag : Arrays.asList(newTags.split(","))){
            if(!uniqueTags.contains(tag.trim())){
                uniqueTags.add(tag.trim());
            }
        }
        
        Collections.sort(uniqueTags);
        
        setTags(uniqueTags.toString().replace("[", "").replace("]", ""));
    }
    
    public void addVideo(PlaylistVideo v) throws FileNotFoundException, UnsupportedEncodingException{
        if(v.getTags().length() > 0){
            addTags(v.getTags());
        }

        videos.add(v);
        
    }
    
    public void deletePlaylist(File folder) throws IOException{
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deletePlaylist(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    
    public boolean contains(String name){
        for(PlaylistVideo video : getPlaylist()){
            if(video.getFilteredName().equals(name)){
                return true;
            }
        }
        return false;
    }
    
    @Override
    // Returns the playlist's name for when it's displayed in the playlist view
    public String toString(){
        return playlistName;
    }
}
