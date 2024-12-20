package com.example.Twitetr.Controller;

//URL FÖR ATT TESTA BACKEND: http://localhost:8080/api/tweets
//http://localhost:8080/api/tweets

import com.example.Twitetr.Entity.Tweet;
import com.example.Twitetr.Service.LibrisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping("/api/tweets")
public class Controller_Twitter {
    private ArrayList<Tweet> tweetList = new ArrayList<>();
    @Autowired
    private LibrisService librisService;

    @GetMapping //Hämtar alla Tweets
    public String getAllTweets() { //testmetod.
        return "Hej hej!";
    }

    /*
    public ArrayList<Tweet> getAllTweets() {
        return tweetList;
    }
     */

    // en metod för att veta vilka otillåtna tecken som finns.
    public boolean containsInvalidCharacters(String tweet){
        String invalidCharsRegex = "[\\x00-\\x1F<>\"'`;]";
        return tweet.matches(".*" + invalidCharsRegex + ".*");
    }

    //testa med: http://localhost:8080/api/tweets/test-manage-tweet
    @GetMapping("/test-manage-tweet")
    public ResponseEntity<HashMap<String, Object>> testManageTweet() {
        // Skapa mock-input
        Map<String, String> testInput = new HashMap<>();
        testInput.put("tweet", "Detta är ett test");
        testInput.put("language", "svenska");

        // Anropa den riktiga manageTweet-metoden
        return manageTweet(testInput);
    }

    @PostMapping("/post-tweet")
    public ResponseEntity<String> postTweet(@RequestBody Map<String, String> userInput){
        String tweet = userInput.get("tweet");

        if(checkIfEmpty(tweet)){
            return ResponseEntity.badRequest().body("The tweet does not exist. Please Try again");
        }

        if(tweetAboveLimit(tweet)){
            return ResponseEntity.badRequest().body("The tweet has more than 280 characters.");
        }

        if(containsInvalidCharacters(tweet)){
            return ResponseEntity.badRequest().body("Error: Tweeten innehåller otillåtna tecken.");
        }

        boolean success = sendToTwitterAPI(tweet);

        if(success){
            return ResponseEntity.ok("The tweet has been sent.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Kunde inte publicera tweeten.");
        }

    }

    //skickar tweet till twitters API
    public boolean sendToTwitterAPI(String tweet){
        try {
            //mock - kod
            System.out.println("Följande tweet skickas till twitters API: " + tweet);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ResponseEntity is a clas used to show HTTP requests, such as status code.
    @PostMapping
    public ResponseEntity<String> addTweet(@RequestBody Tweet tweet) {
        if(tweet.getTweetInformation() == null){
            return ResponseEntity.badRequest().body("The tweet does not exist. Please Try again");
        }
        else if(tweet.getTweetInformation().isEmpty()){
            return ResponseEntity.badRequest().body("The tweet is empty and therefore cannot be sent. Please try again.");
        }

        else {
            tweetList.add(tweet);
            return ResponseEntity.ok("Tweet successfully received: " + tweet.getTweetInformation());
        }
    }

    @PostMapping("/manage-tweet")
    public ResponseEntity<HashMap<String, Object>> manageTweet(@RequestBody Map<String, String> userInput) {
        HashMap<String, Object> spellingControl = new HashMap<>();
        String userTweet = userInput.get("tweet");
        String specified_language = userInput.get("language");

        boolean empty_tweet = checkIfEmpty(userTweet);
        boolean no_language_specified = checkIfEmpty(specified_language);

        if (empty_tweet || no_language_specified) {
            spellingControl.put("Invalid", "The tweet is either empty or no language has been specified");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        //användning av containsInvalidCharacters metoden. Kontrollerar ifall det finns otillåtna tecken och skickar ett meddelande.
        if (containsInvalidCharacters(userTweet)) {

            spellingControl.put("Error", "Tweeten innehåller otillåtna tecken (t.ex. kontrolltecken, specialtecken som <, >, \", ';').");
            return ResponseEntity.badRequest().body(spellingControl);
        }


        //En mock metod (ska ersättas med LIBRIS API senare)
        HashMap<String, Object> librisResponse = librisService.checkSpelling(userTweet, specified_language);

        if (librisResponse.containsKey("suggestions")) {
            spellingControl.put("LIBRIS suggestions", librisResponse.get("suggestions").toString());
        } else {
            spellingControl.put("LIBRIS suggestions", "Inga förslag hittades.");


        }
        spellingControl.put("User original tweet", userTweet);
        return ResponseEntity.ok(spellingControl);
    }

    public boolean checkIfEmpty(String input){
        if(input == null || input.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public boolean tweetAboveLimit(String tweet){
        if(tweet.length() > 280){
            return true;
        } else {
            return false;
        }
    }

    public String suggestedGrammar(String tweet, String language){
        Map<String, String> spellingCorrection = new HashMap<>();
        spellingCorrection.put("Twitetr", "Twitter");
        spellingCorrection.put("proggrammering", "programmering");
        spellingCorrection.put("exampel", "exempel");
        spellingCorrection.put("staavning", "stavning");

        if(tweetAboveLimit(tweet)){
            return("error, överskridit antal tillåtna tecken!");
        }

        String[] words = tweet.split(" ");
        StringBuilder correctedTweet = new StringBuilder();

        for (String word : words) {
            String word_lowerCase = word.toLowerCase();
            if (spellingCorrection.containsKey(word_lowerCase)) {
                correctedTweet.append(spellingCorrection.get(word_lowerCase)).append(" ");
            } else {
                correctedTweet.append(word).append(" ");
            }
        }

        return "Förbättrad tweet med språket (" + language + "): " + correctedTweet.toString().trim();
    }

    public static void main(String[] args) {
        Controller_Twitter controller = new Controller_Twitter();
        String inputTweet = "Twitetr är fantastisk!";
        String correctedTweet = controller.suggestedGrammar(inputTweet, "svenska");
        System.out.println(correctedTweet);
    }

}
