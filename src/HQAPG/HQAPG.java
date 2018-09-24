package hqapg;

import java.io.IOException;
import java.util.ArrayList;

import org.openqa.selenium.WebDriver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

public class HQAPG {
	static final boolean debugMode = false;
	static final String ip = "<YOUR IP HERE>";
	//Insert your bearer token here
	static String bearerToken = "";
	static ArrayList<WebDriver> drivers = new ArrayList<WebDriver>();
	static ArrayList<ChromeThread> chromethreads = new ArrayList<ChromeThread>();
	public static void main(String[] args) throws UnirestException, WebSocketException, IOException {
		WebDriverManager.chromedriver().setup();
		fireUpSocket();
	}
	public static void fireUpSocket() throws UnirestException, WebSocketException, IOException {
		String streamURL = getStreamURL(bearerToken);
		if(streamURL == null) {
			System.out.println("No show right now");
			return;
		}
		 new WebSocketFactory()
        .createSocket(streamURL)
        .addListener(new WebSocketAdapter() {
       	 @Override
       	    public void onTextMessage(WebSocket ws, String message) throws Exception {
       		 //ID 0 is like the botcontroller's websocket
       		 JsonObject jsonobject = new JsonParser().parse(message).getAsJsonObject(); 
       		 if(jsonobject.get("type").getAsString().equals("question")) {
       			 search(message);
       		 }
       		 else if(jsonobject.get("type").getAsString().equals("questionSummary")) {
       			 //closeTabs();
       		 }
       		 else if(jsonobject.get("type").getAsString().equals("broadcastEnded")) {
       			 System.out.println("Broadcast has ended");
       			 closeTabs();
       			 ws.sendClose();
       			 ws.disconnect();
       		 }
       		 else if(!jsonobject.get("type").getAsString().equals("interaction") && !jsonobject.get("type").getAsString().equals("broadcastStats")) {
       			 System.out.println(message);
       		 }
       	 }
        })
        .addHeader("Authorization", "Bearer "+bearerToken)
		 .addHeader("User-Agent", "hq-viewer/1.2.4 (iPhone iOS 11.1.1 Scale/3.00)")
		 .addHeader("x-hq-client", "iOS/1.2.10 b69")
		 .addHeader("x-hq-stk", "MQ==")
		 .addHeader("Host", "api-quiz.hype.space")
		 .addHeader("Connection", "keep-alive")
		 .addHeader("Accept-Encoding", "gzip")
		 //Every 10 seconds for ping
		 .setPingInterval(10 * 1000)
        .connect();
	}
	public static void closeTabs() {
		for(int i=0;i<drivers.size();i++) {
			drivers.get(i).close();
			drivers.remove(i);
		}
	}
	public static void TabOpen(ArrayList<String> queries) {
		int counter = 0;
		for(int i=0;i<queries.size();i++) {
			makeThread(queries.get(i), counter);
			counter = counter+500;
		}
       
	}
	public static void makeThread(String query, int point) {
		ChromeThread chromethread = new ChromeThread(query, point);
		chromethreads.add(chromethread);
		Thread thread = new Thread(chromethread);
		thread.start();
	}
	public static void tabUpdate(ArrayList<String> queries) {
		for(int i=0;i<queries.size();i++) {
			chromethreads.get(i).switchWebsite(queries.get(i));
		}
	}
	public static void search(String message) {
		JsonObject questionObj = new JsonParser().parse(message).getAsJsonObject();
		String question = questionObj.get("question").getAsString().replace("\"", "");
		JsonArray answersArray = questionObj.get("answers").getAsJsonArray();
		String answer1 = answersArray.get(0).getAsJsonObject().get("text").getAsString();
		String answer2 = answersArray.get(1).getAsJsonObject().get("text").getAsString();
		String answer3 = answersArray.get(2).getAsJsonObject().get("text").getAsString();
		String query1 = question+"+"+answer1;
		String query2 = question+"+"+answer2;
		String query3 = question+"+"+answer3;
		ArrayList<String> queries = new ArrayList<String>();
		queries.add(query1);
		queries.add(query2);
		queries.add(query3);
		if(chromethreads.size() > 0) {
			tabUpdate(queries);
		}
		else {
			TabOpen(queries);
		}
	}
	public static String getStreamURL(String BearerToken) throws UnirestException {
		if(debugMode) {
    		return "ws://"+ip+":8080/hqsocket";
    	}
		HttpResponse<String> firstpacket = Unirest.get("https://api-quiz.hype.space/shows/now?type=hq")
		.header("Authorization", "Bearer "+BearerToken)
		.header("User-Agent", "HQ/1.2.10 (co.intermedialabs.hq; build:69; iOS 9.3.4) Alamofire/4.5.1")
		.header("x-hq-client", "iOS/1.2.10 b69")
		.asString();
		JsonObject packetobj = new JsonParser().parse(firstpacket.getBody()).getAsJsonObject();
		String broadcaststream = "";
		try {
		broadcaststream = packetobj.get("broadcast").getAsJsonObject().get("socketUrl").getAsString();
		}
		catch(java.lang.IllegalStateException e) {
			return null;
		}
		return broadcaststream;
	}
}
