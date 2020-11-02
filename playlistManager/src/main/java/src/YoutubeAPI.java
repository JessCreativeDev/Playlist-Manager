/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

/**
 * Sample Java code for youtube.channels.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.File;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class YoutubeAPI {
    // The variables
    private static String API_KEY;
    private static final String APPLICATION_NAME = "PlaylistManager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static int timesCalled = 0; // Keeps track of how many times the class is called for performance purposes
    private static YouTube youtubeService;

    /**
     * Initializes the service obtained from the Youtube API
     * 
     * @param key - The API Key
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public static void getService(String key) throws GeneralSecurityException, IOException, ClassNotFoundException, URISyntaxException {
        
        API_KEY = key;
        
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
    
    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @param id
     * @return 
     * @throws java.security.GeneralSecurityException 
     * @throws java.io.IOException
     * @throws com.google.api.client.googleapis.json.GoogleJsonResponseException
     * @throws java.lang.ClassNotFoundException
     */
    public static PlaylistItemListResponse requestPlaylistItems(String id) throws GeneralSecurityException, IOException, GoogleJsonResponseException, ClassNotFoundException {
        
        timesCalled++;
        System.out.println("Method: RequestPlaylistItems\tTimes Called: " + timesCalled);
        
        // Define and execute the API request
        YouTube.PlaylistItems.List request = youtubeService.playlistItems()
            .list("snippet, contentDetails")
            .setKey(API_KEY)
            .setMaxResults(50L)
            .setPlaylistId(id);
        PlaylistItemListResponse response = request.execute();
        
        return response;
    }
    
    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @param id
     * @param token
     * @return 
     * @throws java.security.GeneralSecurityException 
     * @throws java.io.IOException
     * @throws com.google.api.client.googleapis.json.GoogleJsonResponseException
     * @throws java.lang.ClassNotFoundException
     */
    public static PlaylistItemListResponse requestNextPlaylistItems(String id, String token) throws GeneralSecurityException, IOException, GoogleJsonResponseException, ClassNotFoundException {
        
        timesCalled++;
        System.out.println("Method: RequestNextPlaylistItems\tTimes Called: " + timesCalled);
        
        // Define and execute the API request
        YouTube.PlaylistItems.List request = youtubeService.playlistItems()
            .list("snippet, contentDetails")
            .setKey(API_KEY)
            .setMaxResults(50L)
            .setPageToken(token)
            .setPlaylistId(id);
        PlaylistItemListResponse response = request.execute();
        
        return response;
    }
    
    public static PlaylistListResponse requestPlaylist(String id) throws GeneralSecurityException, IOException, GoogleJsonResponseException, ClassNotFoundException {
        
        timesCalled++;
        System.out.println("Method: RequestPlaylist\tTimes Called: " + timesCalled);
        
        // Define and execute the API request
        YouTube.Playlists.List request = youtubeService.playlists()
            .list("snippet, contentDetails")
            .setKey(API_KEY)
            .setMaxResults(1L)
            .setId(id);
        PlaylistListResponse response = request.execute();
        
        return response;
    }
    
    public static VideoListResponse requestVideo(String id) throws GeneralSecurityException, IOException, GoogleJsonResponseException, ClassNotFoundException {
        
        timesCalled++;
        System.out.println("Method: RequestVideo\tTimes Called: " + timesCalled);
        
        // Define and execute the API request
        YouTube.Videos.List request = youtubeService.videos()
            .list("snippet, contentDetails")
            .setKey(API_KEY)
            .setMaxResults(1L)
            .setId(id);
        VideoListResponse response = request.execute();
        
        return response;
    }
}