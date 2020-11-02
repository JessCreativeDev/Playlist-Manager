/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import javafx.scene.image.Image;

/**
 *
 * @author Thema
 */
public final class PlaylistVideo implements Comparable<PlaylistVideo>{
    
    // The Variables
    
    // The Strings
    private String folderPath;
    private String videoPath;
    private String videoID;
    private String thumbnailURL;
    private String videoName;
    private String filteredVideoName;
    private String videoFileName;
    private String description;
    private String channel;
    
    // The thumbnail image for the video
    private Image thumbnailImage;
    
    // The ints
    private int thumbnailHeight;
    private int thumbnailWidth;
    private int position;
    private int positionBias;
    private int loopCount;
    
    private double speed;
    
    // The list of tags for the video
    private ArrayList<String> tags;
    private File textFile;
    private PrintWriter videoFileWriter;
    private Scanner videoFileReader;
    
    // The Booleans
    private boolean isLooping;
    private boolean isSpeedChanged;
    private boolean isSaved;
    
    // The Default Constructor
    PlaylistVideo(){
        videoID = "";
        thumbnailURL = "";
        videoName = "";
        videoFileName = "";
        description = "";
        channel = "";
        thumbnailHeight = 0;
        thumbnailWidth = 0;
        position = 0;
        positionBias = 0;
        loopCount = 0;
        speed = 1;
        isLooping = false;
        isSpeedChanged = false;
        isSaved = false;
        tags = new ArrayList<>();
    }
    
    PlaylistVideo(String path, String vn) throws IOException{
        
        // After figuring out all of the edge cases for the file names, rename this into the replaces for the file namem, and remake the files.
        filteredVideoName = vn.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();
        folderPath = path + "/" + filteredVideoName;
        textFile = new File(folderPath, "video.txt");
        
        if(textFile.exists()){
            videoFileReader = new Scanner(textFile);
            if(videoFileReader.hasNext()){

                videoFileReader.skip("Video ID: ");
                videoID = videoFileReader.nextLine();

                videoFileReader.skip("Video Name: ");
                videoName = videoFileReader.nextLine();
                
                videoFileName = videoName.replace(":", "_").replace("?", "_").replaceAll("[|\"/\\\\]", "_").replaceAll("[*<>]", "") + ".mp4";
                
                videoPath = new File(folderPath, videoFileName).toURI().toURL().toString();
                
                videoFileReader.skip("Description: ");
                while(!videoFileReader.hasNext("Channel:")){
                    description += videoFileReader.nextLine() + "\n";
                }
                if(description == null){
                    description = "";
                    videoFileReader.nextLine();
                } else if(description.startsWith("null")){
                    description = description.replace("null", "");
                }

                videoFileReader.skip("Channel: ");
                channel = videoFileReader.nextLine();

                videoFileReader.skip("Thumbnail URL: ");
                thumbnailURL = videoFileReader.nextLine();

                try (FileInputStream tempStream = new FileInputStream(folderPath + "/image.jpg")) {
                    thumbnailImage = new Image(tempStream, 60, 45, false, false);
                }

                videoFileReader.skip("Thumbnail Height: ");
                thumbnailHeight = videoFileReader.nextInt();
                videoFileReader.nextLine();

                videoFileReader.skip("Thumbnail Width: ");
                thumbnailWidth = videoFileReader.nextInt();
                videoFileReader.nextLine();

                videoFileReader.skip("Position: ");
                position = videoFileReader.nextInt();
                videoFileReader.nextLine();

                videoFileReader.skip("Postiion Bias: ");
                positionBias = videoFileReader.nextInt();
                videoFileReader.nextLine();

                videoFileReader.skip("Loop Count: ");
                loopCount = videoFileReader.nextInt();
                videoFileReader.nextLine();

                videoFileReader.skip("Speed: ");
                speed = videoFileReader.nextDouble();
                videoFileReader.nextLine();
                
                videoFileReader.skip("Is Video Looping: ");
                isLooping = videoFileReader.nextBoolean();
                videoFileReader.nextLine();

                videoFileReader.skip("Is Video Speed Changed: ");
                isSpeedChanged = videoFileReader.nextBoolean();
                videoFileReader.nextLine();
                
                videoFileReader.skip("Is Video Saved: ");
                isSaved = videoFileReader.nextBoolean();
                videoFileReader.nextLine();

                videoFileReader.skip("Tags: ");
                tags = new ArrayList<>();
                setTags(videoFileReader.nextLine());
            }
        }
    }
    
    // The Normal Constructor
    PlaylistVideo(String path, String id, String vn, String d, String c, String tu, int tw, int th, int p) throws IOException{
        
        filteredVideoName = vn.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();
        folderPath = path + "/" + filteredVideoName;
        textFile = new File(folderPath, "video.txt");
        
        File videoFolder = new File(folderPath);
        videoFolder.mkdir();
        
        textFile = new File(videoFolder.getCanonicalPath(), "video.txt");
        
        if(!textFile.exists()){
            textFile.createNewFile();
        }
        
        videoFileWriter = new PrintWriter(textFile, "UTF-8");
        videoID = id;
        videoFileWriter.println("Video ID: " + id);

        videoName = vn;         
        videoFileName = videoName.replace(":", "_").replace("?", "_").replaceAll("[|\"/\\\\]", "_").replaceAll("[*<>]", "") + ".mp4";
        videoPath = new File(folderPath, videoFileName).toURI().toURL().toString();
        videoFileWriter.println("Video Name: " + vn);

        description = d;
        if(description.startsWith("null")){
            description = description.replace("null", "");
        }
        videoFileWriter.println("Description: " + description);

        channel = c;
        videoFileWriter.println("Channel: " + c);

        thumbnailURL = tu;
        videoFileWriter.println("Thumbnail URL: " + tu);

        try(InputStream in = new URL(thumbnailURL).openStream()){
            Files.copy(in, Paths.get(videoFolder.getCanonicalPath() + "/image.jpg"));
            try (FileInputStream tempStream = new FileInputStream(videoFolder.getCanonicalPath() + "/image.jpg")) {
                thumbnailImage = new Image(tempStream, 60, 45, false, false);
            }
        }
        
        thumbnailHeight = th;
        videoFileWriter.println("Thumbnail Height: " + th);

        thumbnailWidth = tw;
        videoFileWriter.println("Thumbnail Width: " + tw);

        position = p;
        videoFileWriter.println("Position: " + p);

        positionBias = 0;
        videoFileWriter.println("Postiion Bias: " + positionBias);
        
        loopCount = 0;
        videoFileWriter.println("Loop Count: " + loopCount);
        
        speed = 1;
        videoFileWriter.println("Speed: " + speed);
        
        isLooping = false;
        videoFileWriter.println("Is Video Looping: " + isLooping);
        
        isSpeedChanged = false;
        videoFileWriter.println("Is Video Speed Changed: " + isSpeedChanged);
        
        isSaved = false;
        videoFileWriter.println("Is Video Saved: " + isSaved);
            
        tags = new ArrayList<>();
        videoFileWriter.println("Tags: " + "None");
        videoFileWriter.close();
        
    }
    
    /**
     * Replaces the old text in the video's property file with the new text
     * 
     * @param oldText - The text to be replaced
     * @param newText - The new text
     * 
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void replaceText(String oldText, String newText)  throws FileNotFoundException, UnsupportedEncodingException{
        videoFileReader = new Scanner(textFile);
        
        String fileText = "";
        
        while(videoFileReader.hasNext()){
            fileText += videoFileReader.nextLine() + "\n";
        }
        
        fileText = fileText.replace(oldText, newText);
        
        videoFileWriter = new PrintWriter(textFile, "UTF-8");
        videoFileWriter.write(fileText);
        
        videoFileReader.close();
        videoFileWriter.close();
    }
    
    // The Getters
    // Returns the video's folder path
    public String getFolderPath(){
        return folderPath;
    }
    
    // Returns the video's file path
    public String getVideoPath(){
        return videoPath;
    }
    
    // Returns the Video ID
    public String getVideoID() {
        return videoID;
    }
    
    // Returns the Thumbnail URL
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    // Returns the Video Name
    public String getVideoName() {
        return videoName;
    }
    
    public String getVideoFileName() {
        return videoFileName;
    }

    public String getFilteredName(){
        return filteredVideoName;
    }
    
    // Returns the Video Description
    public String getDescription() {
        return description;
    }

    // Returns the Channel Name
    public String getChannel() {
        return channel;
    }
    
    public Image getThumbnail(){
        return thumbnailImage;
    }

    // Returns the Thumbnail Height
    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    // Returns the Thumbnail Width
    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    // Returns the Video's Position
    public int getPosition() {
        return position;
    }
    
    // Returns the Video's position bias
    public int getPositionBias(){
        return positionBias;
    }
    
    // Returns the Video's loop count
    public int getLoopCount(){
        return loopCount;
    }
    
    // Returns the Video's speed
    public double getSpeed(){
        return speed;
    }
    
    // Returns if the Video's Loop Count is changed
    public boolean getIsLooping(){
        return isLooping;
    }
    
    // Returns if the Video's Speed is changed
    public boolean getIsSpeedChanged(){
        return isSpeedChanged;
    }
    
    // Returns if the Video is saved
    public boolean getIsSaved(){
        return isSaved;
    }
    
    // Returns the Video's tags
    public String getTags(){
        return tags.toString().replace("[", "").replace("]", "");
    }

    // The Setters
    // Sets the Video's ID
    public void setVideoID(String id)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Video ID: " + videoID, "Video ID: " + id);
        videoID = id;
    }
    
    // Sets the Video's Name
    public void setVideoName(String vn)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Video Name: " + videoName, "Video Name: " + vn);
        videoName = vn;
    }

    // Sets the Video's Description
    public void setDescription(String d)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Description: " + description, "Description: " + d);
        description = d;
    }

    // Sets the Video's Channel Name
    public void setChannel(String c)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Channel: " + channel, "Channel: " + c);
        channel = c;
    }
    
    // Sets the thumbnail image
    public void setThumbnail(Image i){
        thumbnailImage = i;
    }
    
    // Sets the Thumbnail's URL
    public void setThumbnailURL(String tu)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail URL: " + thumbnailURL, "Thumbnail URL: " + tu);
        thumbnailURL = tu;
    }

    // Sets the Thumbnail's Height
    public void setThumbnailHeight(int th)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail Height: " + thumbnailHeight, "Thumbnail Height: " + th);
        thumbnailHeight = th;
    }

    // Sets the Thumbnail's Width
    public void setThumbnailWidth(int tw)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Thumbnail Width: " + thumbnailWidth, "Thumbnail Width: " + tw);
        thumbnailWidth = tw;
    }

    // Sets the Video's Position
    public void setPosition(int p)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Position: " + position, "Position: " + p);
        position = p;
    }
    
    // Sets the Video's Loop Count
    public void setLoopCount(int lc)  throws FileNotFoundException, UnsupportedEncodingException {
        replaceText("Loop Count: " + loopCount, "Loop Count: " + lc);
        loopCount = lc;
    }
    
    // Sets the Video's Speed
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
    
    // Sets tif the Video's Loop Count is changed
    public void setIsLooping(boolean l)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Is Video Looping: " + isLooping, "Is Video Looping: " + l);
        isLooping = l;
    }
    
    // Sets if the Video's Speed is changed
    public void setIsSpeedChanged(boolean sc)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Is Video Speed Changed: " + isSpeedChanged, "Is Video Speed Changed: " + sc);
        isSpeedChanged = sc;
    }
    
    // Sets if the Video is saved
    public void setIsSaved(boolean s)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Is Video Saved: " + isSaved, "Is Video Saved: " + s);
        isSaved = s;
    }
    
    // Sets the Video's tags
    public void setTags(String newTags)  throws FileNotFoundException, UnsupportedEncodingException{
        if(getTags().isEmpty()){
            replaceText("Tags: None", "Tags: " + newTags);
        } else {
            replaceText("Tags: " + getTags(), "Tags: " + newTags);
        }
        
        // Removes the ['s and ]'s, and splits the string into a list by the commas
        tags = new ArrayList<>(Arrays.asList(newTags.split(",")));
        Collections.sort(tags);
    }
    
    // Sets the Video's position bias
    public void setPositionBias(int pb)  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Position Bias: " + positionBias, "Position Bias: " + pb);
        positionBias = pb;
    }
    
    // Increases the Video's position bias by 1
    public void incrementBias()  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Position Bias: " + positionBias, "Position Bias: " + (positionBias + 1));
        positionBias++;
    }
    
    // Decreases the Video's position bias by 1
    public void decrementBias()  throws FileNotFoundException, UnsupportedEncodingException{
        replaceText("Position Bias: " + positionBias, "Position Bias: " + (positionBias - 1));
        positionBias--;
    }
    
    @Override
    // Returns the video's name for when it's displayed in the playlist view
    public String toString(){
        return videoName;
    }

    // Deletes the video file from the folder
    public void deleteVideo(){
        File video = new File(getVideoPath().replace("file:/", "").replace("%20", " "));
        if(video.exists() && !getVideoPath().isEmpty()){
            video.delete();
        }
    }
    
    // Deletes the video's folder
    public void deleteFolder(File folder) throws IOException{
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    
    @Override
    public int compareTo(PlaylistVideo otherVideo) {
        if(getPosition() > otherVideo.getPosition()){
            return 1;
        } else if (getPosition() < otherVideo.getPosition()){
            return -1;
        } else {
            return 0;
        }
    }
    
}
