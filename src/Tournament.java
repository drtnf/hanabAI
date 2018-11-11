import hanabAI.*;
import agents.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class Tournament{

  private String[] agentNames = {"BasicAgent","ModelAgent"};
  private Hashtable<String,Constructor> agents;
  private Hashtable<String,Statistics> scoreboard;
  private boolean html = false;
  static private PrintStream out;
  private static final int ind = 20;
  private static final int three = 100;
  private static final int four = 100;
  private static final int five = 100;
  Random rand = new Random();

  public Tournament(boolean html, String agentList){
    try{
      BufferedReader in = new BufferedReader(new FileReader(agentList));
      HashSet<String> tmpAgents = new HashSet<String>();
      String s = in.readLine();
      while(s!=null){
        if(s.charAt(0)!='#') tmpAgents.add(s.trim());
        s = in.readLine();
      }
      agentNames = tmpAgents.toArray(new String[0]);
    }
    catch(Exception e){e.printStackTrace();}
    agents = new Hashtable<String,Constructor>();
    scoreboard = new Hashtable<String,Statistics>();
    for(String s: agentNames){
      try{
        System.out.println(s);
        Class c = Class.forName("agents."+s);
        agents.put(s, c.getConstructors()[0]);
        scoreboard.put(s, new Statistics(s, (agents.get(s).newInstance()).toString()));
      }catch(Exception e){e.printStackTrace();System.exit(0);}
    }
  }
 
  public Tournament(){
    this(false, "Agents.txt");
  }

  class GameThread extends Thread{
    Agent[] ags;
    int[] stds;
    int type; //0 for ind, 1 for 3, 2 for 4 , 3 for 5

    public GameThread(Agent[] ags, int[] stds, int type){
      this.ags = ags;
      this.stds = stds;
      this.type = type;
    }

    public void run(){
      StringBuffer sb = new StringBuffer();
        Hanabi game = new Hanabi(ags);
        int sc = game.play(sb);
        for(int i = 0; i<stds.length; i++){
         if(type==0){
           scoreboard.get(agentNames[stds[i]]).individualScore+= sc;
           scoreboard.get(agentNames[stds[i]]).individualGames++;
         }
         else{
           scoreboard.get(agentNames[stds[i]]).scores[type-1]+= sc;
           scoreboard.get(agentNames[stds[i]]).games[type-1]++;
         }
        }
        sb.append("The final score was "+sc+"\n");
        sb.append(Hanabi.critique(sc));
        System.out.println(sb);
    }
  }

  public void individualGames(int numPlayers,int howMany){
    for(int i = 0; i<agentNames.length; i++){
      //  System.out.println(agentNames[i]);
      for(int j = 0; j<howMany; j++){
        StringBuffer sb = new StringBuffer();
        Constructor c = agents.get(agentNames[i]);
        Agent[] ags = new Agent[numPlayers];
        int[] stds = new int[numPlayers];
        for(int k = 0; k<numPlayers; k++) try{
          ags[k] = (Agent)c.newInstance();
          stds[k] = i;
        }catch(Exception e){e.printStackTrace();System.exit(0);}
        GameThread g = new GameThread(ags,stds,0);
        g.start();
      }
    }
  }


  public void playGames(int numPlayers, int howMany){
    int count = 0;
    while(count<agentNames.length*howMany){
      int[] players = new int[numPlayers];
      try{
      players[0] = rand.nextInt(agentNames.length);
      while(scoreboard.get(agentNames[players[0]]).games[numPlayers-3]>howMany) players[0] = rand.nextInt(agentNames.length);
      for(int i = 1; i<numPlayers; i++) players[i] = rand.nextInt(agentNames.length);
      Agent[] ags = new Agent[numPlayers];
      for(int i = 0; i<numPlayers; i++) try{
        System.out.println(agentNames[players[i]]);
        ags[i] = (Agent)agents.get(agentNames[players[i]]).newInstance();

      }catch(Exception e){e.printStackTrace();System.exit(0);}
      GameThread g = new GameThread(ags,players,numPlayers-2);
      g.start();
      for(int i = 0;i<numPlayers; i++) 
        if(scoreboard.get(agentNames[players[i]]).games[numPlayers-3]<howMany)
          count++;
      }catch(Exception e){
        e.printStackTrace();
        for(int i = 0; i<players.length; i++){
          System.out.println("player "+i+":"+players[i]);
          System.out.println(scoreboard.get(agentNames[players[i]]));
        }
      }
    }
  }

  


  public String toString(){
    Statistics[] scores = new Statistics[agentNames.length];
    for(int i = 0; i<agentNames.length; i++) scores[i] = scoreboard.get(agentNames[i]);
    Arrays.sort(scores);
    String s = "";
    if(html){
      s ="<html><head><title>CITS3001 Hanabi Tournment</title></head><body><h1>CITS3001 Hanabi Tournament</h1>\nStandings:\n";
      s+="<table><hr><td>Student Number</td><td>Ind. Score</td><td>3 Score</td><td>4 Score</td><td>5 Score</td><td>Average</td></hr>\n";
      for(int i = scores.length; i>0; i--)s+=scores[i-1].toString();
      s+="\n</table></body></html>";
    }
    else{
      s+="Student Number,Agent Name, Ind Score,3 Score,3 Games,4 Score,4 Games,5 Score,5 Games, Average\n";
      for(int i = scores.length; i>0; i--)s+=scores[i-1].toString();
      s+="\n";
    } 
    return s;
  }

  public void setHTML(boolean html){
    this.html = html;
  }

  public static void main(String args[]){
    if(args.length>1)
      try{out = new PrintStream(args[1]);}catch(Exception e){e.printStackTrace();}
    else out = System.out;
    String agentNames ="Agents.txt";
    if(args.length!=0) agentNames = args[0];
    Tournament t = new Tournament(false, agentNames);
    t.individualGames(4, ind);
    while(Thread.currentThread().activeCount()>1) try{Thread.currentThread().sleep(1000);}catch(Exception e){e.printStackTrace();}
    try{Thread.currentThread().sleep(3000);}catch(Exception e){e.printStackTrace();}
    t.playGames(3, three);
    while(Thread.currentThread().activeCount()>1) try{Thread.currentThread().sleep(1000);}catch(Exception e){e.printStackTrace();}
    try{Thread.currentThread().sleep(3000);}catch(Exception e){e.printStackTrace();}
    t.playGames(4, four);
    while(Thread.currentThread().activeCount()>1) try{Thread.currentThread().sleep(1000);}catch(Exception e){e.printStackTrace();}
    try{Thread.currentThread().sleep(3000);}catch(Exception e){e.printStackTrace();}
    t.playGames(5, five);
    while(Thread.currentThread().activeCount()>1) try{Thread.currentThread().sleep(1000);}catch(Exception e){e.printStackTrace();}
    try{
      System.out.println("Scores:"+t.toString());
      t.setHTML(true);
      out.println(t.toString());
      t.setHTML(false);
      out.println(t.toString());
    }catch(Exception e){
      for(int i = 0; i<t.agentNames.length; i++){
        System.out.println(i+":"+t.agentNames[i]+":"+t.scoreboard.get(t.agentNames[i]));
      }
    }
  }

class Statistics implements Comparable<Statistics>{

  public String number;//student number
  public String name;//from the agent get name method
  public int individualGames;//how many times the agent has played an individiual game
  public int individualScore;// cumulative score of all individual games
  public int[] games;//How many times the agent has played a i+3 player game
  public int[] scores;//the cumulative score in the i+3 games

  public Statistics(String number,String name){
    this.number = number;
    this.name = name;
    games = new int[3];
    scores = new int[3];
  }

  public String toString(){
    String s = "";
    if(html){
      s = "<tr><td>";
      s+=number+"</td><td>";
      s+=name+"</td><td>";
      s+= String.format(" %.3f </td><td>",(1.0*individualScore)/individualGames);
      s+= String.format(" %.3f </td><td>",(1.0*scores[0])/games[0]);
      s+= String.format(" %.3f </td><td>",(1.0*scores[1])/games[1]);
      s+= String.format(" %.3f </td><td>",(1.0*scores[2])/games[2]);
      s+= String.format(" %.3f </td><td>",(1.0*(scores[0]+scores[1]+scores[2]))/(games[0]+games[1]+games[2]));
      s+="</tr>\n";
    }
    else{
      s =number+",";
      s+=name+",";
      s+= String.format(" %.3f ,",(1.0*individualScore)/individualGames);
      s+= String.format(" %.3f ,",(1.0*scores[0])/games[0]);
      s+= String.format(games[0]+",");
      s+= String.format(" %.3f ,",(1.0*scores[1])/games[1]);
      s+= String.format(games[1]+",");
      s+= String.format(" %.3f ,",(1.0*scores[2])/games[2]);
      s+= String.format(games[2]+",");
      s+= String.format(" %.3f",(1.0*(scores[0]+scores[1]+scores[2]))/(games[0]+games[1]+games[2]));
      s+="\n";
    }
    return s;
  }
     
  public double getScore(){return (1.0*(scores[0]+scores[1]+scores[2]))/(games[0]+games[1]+games[2]);}

  public int compareTo(Statistics stat){
    return getScore()>stat.getScore()?1:-1;
  }

  }
}
