package com.example.Twitetr.Controller;

//URL FÖR ATT TESTA BACKEND: http://localhost:8080/api/tweets

import com.example.Twitetr.Entity.Tweet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/tweets")
public class Controller_Twitter {
    private ArrayList<Tweet> tweetList = new ArrayList<>();

    @GetMapping //Hämtar alla Tweets
    public String getAllTweets() { //testmetod.
        return "Hej hej!";
    }

    /*
    public ArrayList<Tweet> getAllTweets() {
        return tweetList;
    }
     */


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
    public ResponseEntity<HashMap<String, String>> manageTweet(@RequestBody Map<String, String> userInput){
        HashMap<String, String> spellingControl = new HashMap<>();
        String userTweet = userInput.get("Tweet");
        String specified_language = userInput.get("Language");

        boolean empty_tweet = checkIfEmpty(userTweet);
        boolean no_language_specified = checkIfEmpty(specified_language);

        if(empty_tweet || no_language_specified){
            spellingControl.put("Invalid", "The tweet is either empty or no language has been specified");
            return ResponseEntity.badRequest().body(spellingControl);
        }

        //En mock metod (ska ersättas med LIBRIS API senare)
        String tweet_improvement = suggestedGrammar(userTweet, specified_language);

        spellingControl.put("User original tweet", userTweet);
        spellingControl.put("Suggested grammar", tweet_improvement);
        return ResponseEntity.ok(spellingControl);
    }

    public boolean checkIfEmpty(String input){
        if(input == null || input.isEmpty()){
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

        String[] words = tweet.split(" ");
        StringBuilder correctedTweet = new StringBuilder();

        for (String word: words) {
            if (spellingCorrection.containsKey(word)) {
                correctedTweet.append(spellingCorrection.get(word)).append(" ");
            } else {
                correctedTweet.append(word).append(" ");
            }
        }

        return "Förbättrad tweet med språket (" + language + "): " + tweet.replace("Twitetr", "Twitter");
    }

    public static void main(String[] args) {
        Controller_Twitter controller = new Controller_Twitter();
        String inputTweet = "Twitetr är fantastisk!";
        String correctedTweet = controller.suggestedGrammar(inputTweet, "svenska");
        System.out.println(correctedTweet);
    }

}
