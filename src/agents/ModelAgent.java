package agents;
import hanabAI.*;
import java.io.*;


import java.util.*;


/**
 * A model agent for playing Hanabi.
 *@author Tim French 
 **/
public class ModelAgent implements Agent{

  //knowledge matrix where knowledge[i][j][k] is the number of possibilities for agents i's jth card to be the kth card in the deck.
  private int[][][] knowledge;
  //remaining cards matrix where remaining [i][j] is the maximum number of cards j, agent i has not seen.
  private int[][] remaining;
  //number of cards remaining in PLay (in the deck or in someone's hand AND playable)
  private int[] inPlay; 
  //firework max numbers, indexed by colour to int
  private int[] firework;
  //hands of each player
  private Card[][] hands;
  //hints remaining
  private int hints;
  //fuse remaining
  private int fuse;
  //number of players
  private int numPlayers;
  //number of cards in each hand
  private int numCards;
  //this players index.
  private int index;

  //tuning variables
  private double hintScale = 0.3;    //value of hints
  private double fuseScale = 1.0;
  private double knowScale = 0.5;
  private double playScale = 1.0;    //cost of discarding needed cards
  private double caution = 0.3;      //(0.1-0.33)
  private double frontweighted;
  /**
   * Default constructor, does nothing.
   * **/
  public ModelAgent(){}


  public ModelAgent(double hint, double fuse, double know, double play, double caution, double front){
    hintScale = hint;
    fuseScale = fuse;
    knowScale = know;
    playScale= play;
    this.caution = caution;
    frontweighted = front;
  }

  /**
   * Initialises variables on the first call to do action.
   * @param s the State of the game at the first action
   **/
  public void init(State s){
    numPlayers = s.getPlayers().length;
    numCards = (numPlayers>3?4:5);
    knowledge = new int[numPlayers][(numCards)][25];
    remaining = new int[numPlayers][25];
    hands = new Card[numPlayers][numCards];
    inPlay = new int[25];
    for(int i = 0; i<25; i++)
      inPlay[i] = (i%5==0?3:i%5==4?1:2);
    firework = new int[5];

    //playable = new HashMap<Colour, Integer>();
    //for(Colour c: Colour.getValues())playable.put(c,1);
    for(int i =0; i<numPlayers; i++){
      for(Card c: Card.getDeck())
        remaining[i][cardIndex(c)] = c.getCount();
    }
    for(int i = 0; i<numPlayers; i++){
      if(i!=index){
        hands[i] = s.getHand(i);
        for(int j = 0; j<numCards; j++)
          for(int k = 0;k<numPlayers; k++)
            if(k!=i) remaining[k][cardIndex(hands[i][j])]--;
      }
    }
    for(int i = 0; i< numPlayers; i++)
      for(int j = 0; j<numCards; j++)
        for(int k = 0; k<25; k++)
          knowledge[i][j][k] = remaining[i][k];
    hints = 8;
    fuse = 3; 
  }
 
  //for debugging, displays the current state of knowledge during play
  public String knowledgeString(){
    String s = "";
    for(int i = 0; i< numPlayers; i++){
      s+="\nPlayer:"+i;
      s+="\nCard";
      for(int j = 0; j<numCards; j++)s+="\t"+(j+1);
      s+="\tRemain\tinPlay";
      for(int k= 0; k<25; k++){
        s+="\n"+k/5+"-"+((k%5)+1)+"\t";
        for(int j = 0; j<numCards; j++)s+=knowledge[i][j][k]+"\t";
        s+=remaining[i][k]+"\t\t";
        s+=inPlay[k];
      }
    }
    return s;
  }


  /**
   * Returns the name Threshold.
   * @return the String "Threshold"
   * */
  public String toString(){return "Threshold";}


  /**
   * Performs an action given a state.
   * First, takes an action and extracts all information from it
   * @param s the current state of the game.
   * @return the action the player takes.
   **/ 
  public Action doAction(State s){
    if(knowledge==null){
      index = s.getNextPlayer();
      State t = s;
      while(t.getPreviousState()!=null)t = t.getPreviousState();
      init(t);
    } 
    //get any hints
    updateKnowledge(s);
    LittleState ell = new LittleState(s);
    return ell.search();
  }

 private int[] remainingTot(){
   int[] count = new int[numPlayers];
   String s = "";
   for(int i=0; i<numPlayers; i++)
     for(int k=0; k<25; k++)
       count[i]+=remaining[i][k];
   return count;
 }


  private void updateKnowledgePD(Action a,Card played, Card pickup){
    //update knowledge if index plays
    if(a.getPlayer()==index){
      for(int i=0; i<numPlayers; i++){
        remaining[i][cardIndex(played)]--;
        for(int j = 0; j<numCards; j++)
          if(knowledge[i][j][cardIndex(played)]>0) knowledge[i][j][cardIndex(played)]--;
      }
    }
    //update when someone else plays
    else{
      remaining[a.getPlayer()][cardIndex(played)]--;
      for(int j = 0; j<numCards; j++)
        if(knowledge[a.getPlayer()][j][cardIndex(played)]>0) knowledge[a.getPlayer()][j][cardIndex(played)]--;
      if(pickup!=null){
        int pickupIndex = cardIndex(pickup);
        for(int i=0; i<numPlayers; i++){
          if(i!=a.getPlayer()){                      
            remaining[i][pickupIndex]--;
            for(int j=0; j<numCards; j++)
              if(knowledge[i][j][pickupIndex]>0) knowledge[i][j][pickupIndex]--;
          }
        }
      }
    }
    try{
      knowledge[a.getPlayer()][a.getCard()]=remaining[a.getPlayer()].clone();
    }catch(Exception e){e.printStackTrace();}//never going to happen
  } 

  //updates knowledge arrays given all recent actions.
  public void updateKnowledge(State s){
    try{
      State t = s;
      Stack<State> recents = new Stack<State>();
      for(int i = 0; i<Math.min(numPlayers,s.getOrder());i++){
        recents.push(t);
        t = t.getPreviousState();
      }
      while(!recents.isEmpty()){
        t = recents.pop();
        Action a = t.getPreviousAction();
        switch(a.getType()){
          case PLAY:
            Card played = t.previousCardPlayed();
            int cPI = played.getColour().ordinal();
            int vPI = played.getValue();
            //update fireworks, fuse and inPlay
            if(firework[cPI]==vPI){//successful
              firework[cPI]++;
              inPlay[cardIndex(played)] = 0;
            }
            else{//failed
              fuse--;
              if(inPlay[cardIndex(played)]>0)inPlay[cardIndex(played)]--;
              if(inPlay[cardIndex(played)]==0 && firework[cPI]<vPI){
                for(int i = cardIndex(played)+1; i%5!=0; i++){
                  inPlay[i]=0;
                }
              }
            }
            //update everyones knowledge
            Card pickup = (a.getPlayer()!=index?t.getHand(a.getPlayer())[a.getCard()]:null);
            updateKnowledgePD(a,played, pickup);
            break;
          case DISCARD:
            hints++;
            played = t.previousCardPlayed();
            cPI = played.getColour().ordinal();
            vPI = played.getValue();
            if(inPlay[cardIndex(played)]>0)inPlay[cardIndex(played)]--;
            if(inPlay[cardIndex(played)]==0 && firework[cPI]<vPI){
              for(int i = cardIndex(played)+1; i%5!=0; i++){
                inPlay[i]=0;
              }
            }
            pickup = (a.getPlayer()!=index?t.getHand(a.getPlayer())[a.getCard()]:null);
            updateKnowledgePD(a,played, pickup);
            break;
          case HINT_COLOUR:
            hints--;
            boolean[] hint = a.getHintedCards();
            int col = a.getColour().ordinal();
            for(int j=0; j<hint.length; j++)
              for(int c = 0; c<25; c++)
               if(c<5*col ||c>=5*(col+1)) knowledge[a.getHintReceiver()][j][c] = hint[j]?0:knowledge[a.getHintReceiver()][j][c];
               else knowledge[a.getHintReceiver()][j][c] = hint[j]?knowledge[a.getHintReceiver()][j][c]:0;
            break;
          case HINT_VALUE:
            hints--;
            hint = a.getHintedCards();
            int val = a.getValue();
            for(int j=0; j<hint.length; j++)
              for(int c = 0; c<25; c++)
               if(c%5+1!=val) knowledge[a.getHintReceiver()][j][c] = hint[j]?0:knowledge[a.getHintReceiver()][j][c];
               else knowledge[a.getHintReceiver()][j][c] = hint[j]?knowledge[a.getHintReceiver()][j][c]:0;
            break;
        }//Note: also option to cut down optons where a card is known in one position, it should be removed from others.
      }
    }
    catch(IllegalActionException e){e.printStackTrace();}
  }

  //For searchingthe consequence of different actions...
  //To depth num players:
  //  try:
  //     play Card if known (or likely) to be playable 1 or 2?
  //     hint playable card (1 of 5, always go with 1st)
  //     discard (1 of 5)
  //  branching degree 10
  //  depth 5 equals 10,000
  //

  private class LittleState{
    
    int[] fireworks;
    int[][] hands;//leave in null values
    int[][][] knowledge;
    //the count of needed cards
    int[] inPlay;
    int hints;
    int fuse;
    int player;
    int remainingMoves;

    public LittleState(State s){
     fireworks = new int[5];
     for(Colour c: Colour.values()){ 
       Stack<Card> fw = s.getFirework(c);
       fireworks[c.ordinal()] = fw.isEmpty()?0:fw.peek().getValue();
     }//!!counting from 1
     hands = new int[numPlayers][numCards];
     for(int i =0; i< numPlayers; i++)
       for(int j = 0; j<numCards; j++)
         hands[i][j] = s.getHand(i)[j]!=null?cardIndex(s.getHand(i)[j]):-1;
     knowledge = new int[numPlayers][numCards][25];//we allow knowledge to be negative to aid resetting
     for(int i = 0; i<numPlayers; i++){
       for(int j = 0; j<numCards; j++){
         for(int k = 0; k<25; k++){
           knowledge[i][j][k] = ModelAgent.this.knowledge[i][j][k];
         }
       }
     }
     inPlay = (int[])ModelAgent.this.inPlay.clone();
     hints = s.getHintTokens();
     fuse = s.getFuseTokens();
     player = s.getNextPlayer();
     if(s.getFinalActionIndex()!=-1)
       remainingMoves = s.getFinalActionIndex()-s.getOrder();//the depth to explore to
     else remainingMoves = -1;
    }

    //evaluation function for deepest states.
    public double evaluate(){
      double eval = 0.0;
      //add in score
      for(int i = 0; i<5; i++)eval+=fireworks[i];
      //scale??
      if(remainingMoves!=-1) return eval;//final round, only score matters
      //2 hints make a playable card, but in a long time....
      eval+= hints*hintScale;
      //fuses allow risky guesses....
      eval+= (fuse==0?-100:fuseScale*fuse);
      //evaluate knowledge, for each card in each persons hand add:
      //if the card is playable, how likely that they think it is playable
      for(int i = 0; i<numPlayers; i++){
        if(i!=index){
          for(int j = 0; j<numCards; j++){
            double p = 0.0;
            double a = 0.0;
            if(hands[i][j]!=-1 && playable(hands[i][j])){
              for(int k = 0; k<25; k++){
                if(playable(k)) p+=Math.max(knowledge[i][j][k],0);
                a+=Math.max(knowledge[i][j][k],0);
              }
              eval+=knowScale*p/a;
            }
          }
        }
      }
      //penalise bad discards, returns average number of cards playable. Starts at 2 and comes down. 
      for(int i = 0; i<25; i++) eval+=(playScale*inPlay[i])/25;
      return eval;
    }

    private boolean playable(int i){
      return fireworks[i/5]==i%5;
    }

    //assumes that this is called by index
    public Action search(){
      int depth = (remainingMoves==-1?numPlayers: remainingMoves);
      Action best = null;//set default action guaranteed to be legal
      if(hints>0){
        int other = (index+1)%numPlayers;
        int i = 0;
        while(i<numCards && hands[other][i]==-1)i++;
        int val = hands[other][i]%5+1;
        boolean[] cards = new boolean[numCards];
        while(i<numCards){cards[i]=(hands[other][i]%5+1)==val; i++;}
        try{best = new Action(index, ModelAgent.this.toString(), ActionType.HINT_VALUE, other, cards, val);}
        catch(Exception e){e.printStackTrace();}//never going to happen
      }
      else{
        try{best = new Action(index, ModelAgent.this.toString(),ActionType.DISCARD,0);}
        catch(Exception e){e.printStackTrace();}//never going to happen
      }
      double eval = 0.0;
      double tmp = 0.0;
      for(int i = 0; i<numCards; i++){
        tmp = searchPlay(i,depth);
        if(tmp>eval){
          eval = tmp;
          try{best = new Action(index,ModelAgent.this.toString(),ActionType.PLAY, i);}
          catch(Exception e){e.printStackTrace();}//never going to happen
        }
        tmp = searchDiscard(i, depth); 
        if(tmp>eval){
          eval = tmp;
          try{best = new Action(index,ModelAgent.this.toString(),ActionType.DISCARD, i);}
          catch(Exception e){e.printStackTrace();}//never going to happen
        }
      }
      for(int other = (index+1)%numPlayers;other!=index; other=(other+1)%numPlayers){ 
        for(int card= 0; card< numCards; card++){
          tmp = searchHintColour(card, other, depth);
          if(tmp>eval){
            eval = tmp;
            int col = hands[other][card]/5;
            boolean[] cards = new boolean[numCards];
            for(int i = 0; i<numCards; i++)if(hands[other][i]!=-1) cards[i]=hands[other][i]/5==col;
            try{best = new Action(index, ModelAgent.this.toString(), ActionType.HINT_COLOUR, other, cards, Colour.values()[col]);}
            catch(Exception e){e.printStackTrace();}//never going to happen
          }
          tmp = searchHintValue(card, other, depth);
          if(tmp>eval){
            int val = hands[other][card]%5+1;
            boolean[] cards = new boolean[numCards];
            for(int i = 0; i<numCards; i++)if(hands[other][i]!=-1) cards[i]=hands[other][i]%5+1==val;
            try{best = new Action(index, ModelAgent.this.toString(), ActionType.HINT_VALUE, other, cards, val);}
            catch(Exception e){e.printStackTrace();}//never going to happen
          }
        }
      }
      return best;
    }

    private double search(int depth){
      if(depth<=0) return evaluate();
      double eval = 0.0;
      double tmp = 0.0;
      for(int i = 0; i<numCards; i++){
        tmp = searchPlay(i,depth);
        if(tmp>eval) eval = tmp;
        tmp = searchDiscard(i, depth); 
        if(tmp>eval) eval = tmp;
      }
      if(hints!=0){
        for(int other = (player+1)%numPlayers;other!=player; other=(other+1)%numPlayers){
          if(other!=index){ 
            for(int card= 0; card< numCards; card++){
              tmp = searchHintColour(card, other, depth);
              if(tmp>eval) eval = tmp;
              tmp = searchHintValue(card, other, depth);
              if(tmp>eval) eval = tmp;
            }
          }
        }
      }
      return frontweighted*evaluate()+(1-frontweighted)*eval;
    }

    //card is index of card in players hand
    //depth is depth to search to
    private double searchPlay(int card, int depth){
      //get probabilities of playable
      int played = hands[player][card];
      double scale = 0.0;
      int countGood = 0;
      int countBad = 0;
      for(int i = 0; i<25; i++){
        if(playable(i))countGood+=Math.max(knowledge[player][card][i],0);
        else countBad+=Math.max(knowledge[player][card][i],0);
      }
      if(countGood == 0) return 0.0;
      scale = (countGood*1.0)/(countGood+countBad);
      if(scale< (0.95+caution*(1-fuse))) return 0.0;
      int playedGood = 0;
      //pick a weighted random good card and random bad card for index.
      if(player== index){
        double rand = Math.random();
        double prob = 0.0;
        played = 0;
        while(prob<rand && played<25){
          if(playable(played))prob+= (1.0*Math.max(knowledge[index][card][played],0))/countGood;
          if(prob<rand) played++;
        }
      }
      if(playable(played))
        return scale*searchPlaySuccess(card, played, depth);
      else return (1-scale)*searchPlayFail(card, played, depth);//might need a change
    }

    //when evaluating subtrees, we scale the evaluation of the subtree by how likely it is a rational player would make that move.
    //This is not normalised.!!!!


    private double searchPlaySuccess(int card, int played, int depth){
      //update firework
      firework[played/5]++;
      //update everyones knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
         knowledge[i][j][played]--; 
      //update inPlay
      int inPlayTmp = inPlay[played];
      inPlay[played] = 0;
      //update hints if 5
      if(played%5==4)hints++;
      //update player
      player = (player+1)%numPlayers;
      //recurse
      double eval = search(depth-1);
      //reset player
      player = (player+numPlayers-1)%numPlayers;
      //reset hints if 5
      if(played%5==4)hints--;
      //reset inPlay
      inPlay[played] = inPlayTmp;
      //reset everyones knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
          knowledge[i][j][played]++;
      //reset firework
      firework[played/5]--;
      return eval;
    }

    private double searchPlayFail(int card, int played, int depth){
      //update everyones knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
         knowledge[i][j][played]--; 
      //update fuse
      fuse--;
      //update inPlay
      int[] inPlayTmp = new int[5];
      for(int i = 0; i<5; i++)
        inPlayTmp[i] = inPlay[played-(played%5)+i];
      if(inPlay[played]>0){ 
        inPlay[played]--;
        if(inPlay[played]==0)
          for(int i = played; i%5!=0; i++)
            inPlay[i]=0;
      }
      //update player
      player = (player+1)%numPlayers;
      //recurse
      double eval = search(depth-1);
      //reset player
      player = (player+numPlayers-1)%numPlayers;
      //reset inPlay
      for(int i = 0; i<5; i++)inPlay[5*(played/5) + i] = inPlayTmp[i];
      //reset fuse
      fuse++;
      //reset everyone's knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
          knowledge[i][j][played]++;
      return eval;
    }

    //rational player would not discard playable or inPlay = 1
    private double searchDiscard(int card, int depth){
      if(hints==8) return 0.0;
      //calculate random card for index,
      //and for others scale by not playable and not inPlay=1
      int played = hands[player][card];
      double scale = 1.0;
      int countGood = 0;
      int countBad = 0;
      for(int i = 0; i<25; i++){
        if(!playable(i)&&inPlay[i]!=1)countGood+=Math.max(knowledge[player][card][i],0);
        else countBad+=knowledge[player][card][i];
      }
      if(countGood+countBad == 0) return 0.0;
      scale = countGood*(1.0)/(countGood+countBad);
      if(scale<0.5) return 0.0;
      if(player== index){//grab a random card
        double rand = Math.random();
        double prob = 0.0;
        played = -1;
        while(prob<rand && played<24)prob+= (1.0*Math.max(knowledge[index][card][++played],0))/(countGood+countBad);
      } 
      //update everyones knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
         knowledge[i][j][played]--; 
      //update hints
      hints++;
      //update inPlay
      int[] inPlayTmp = new int[5];
      for(int i = 0; i<5; i++)
        inPlayTmp[i] = inPlay[played-(played%5)+i];
      if(inPlay[played]>0){ 
        inPlay[played]--;
        if(inPlay[played]==0)
          for(int i = played; i%5!=0; i++)
            inPlay[i]=0;
      }
      //update player
      player = (player+1)%numPlayers;
      //recurse
      double eval = search(depth-1);
      //reset player
      player = (player+numPlayers-1)%numPlayers;
      //reset inPlay
      for(int i = 0; i<5; i++)inPlay[5*(played/5) + i] = inPlayTmp[i];
      //reset hints
      hints--;
      //reset everyone's knowledge
      for(int i = 0; i<numPlayers; i++)
        for(int j = 0; j<numCards; j++)
          knowledge[i][j][played]++;
      return scale*eval;
    }


    //rational players only hint playable and inPlay=1
    //no one hints index
    private double searchHintColour(int card, int other, int depth){
      if(hints==0)return 0.0;
      if(hands[other][card]!=-1 && !playable(hands[other][card])&& inPlay[hands[other][card]]!=1)return 0.0;
      else{
        //update players knowledge - store old knowledge as temp
        int[][] tmpKnowledge = new int[numCards][25];
        boolean[] hinted = new boolean[numCards];
        for(int i = 0; i< numCards; i++) hinted[i] = hands[other][i]/5 == hands[other][card]/5;
        for(int i = 0; i< numCards; i++){
          tmpKnowledge[i] = (int[])knowledge[other][i].clone();
          for(int j = 0; j<25; j++)
            if(j/5 != hands[other][card]/5) knowledge[other][i][j] = hinted[i]?0:knowledge[other][i][j];
            else knowledge[other][i][j] = hinted[i]?knowledge[other][i][j]:0;
        }
        //update hints
        hints--;
        //update player
        player = (player+1)%numPlayers;
        //recurse
        double eval = search(depth-1);
        //reset player
        player = (player+numPlayers-1)%numPlayers;
        //reset hints
        hints++;
        //reset players knowledge
        for(int i = 0; i< numCards; i++)
          if(hinted[i]) knowledge[other][i] = tmpKnowledge[i];
        return eval;
      }
    }

    //rational players only hint playable and inPlay=1
    //no one hints index
    private double searchHintValue(int card, int other, int depth){
      if(hints==0)return 0.0;
      if(hands[other][card]!=-1 && !playable(hands[other][card]) && inPlay[hands[other][card]]!=1)return 0.0;
      else{
        //update players knowledge - store old knowledge as temp
        int[][] tmpKnowledge = new int[numCards][25];
        boolean[] hinted = new boolean[numCards];
        for(int i = 0; i< numCards; i++) hinted[i] = hands[other][i]%5 == hands[other][card]%5;
        for(int i = 0; i< numCards; i++){
          tmpKnowledge[i] = (int[])knowledge[other][i].clone();
          for(int j = 0; j<25; j++)
            if(j%5 != hands[other][card]%5) knowledge[other][i][j] = hinted[i]?0:knowledge[other][i][j];
            else knowledge[other][i][j] = hinted[i]?knowledge[other][i][j]:0;
        }
        //update hints
        hints--;
        //update player
        player = (player+1)%numPlayers;
        //recurse
        double eval = search(depth-1);
        //reset player
        player = (player+numPlayers-1)%numPlayers;
        //reset hints
        hints++;
        //reset players knowledge
        knowledge[other] = tmpKnowledge;
        return eval;
      }
    }
  }

  private static int cardIndex(Card c){
    return 5*c.getColour().ordinal()+c.getValue()-1;
  }


  /**
   * This main method is provided to run a simple test game with provided agents.
   * The agent implementations should be in the default package.
   * */
  public static void main(String[] args){
    double hintScale = 0.3;    //value of hints
    double fuseScale = 1.0;
    double knowScale = 1.0;
    double playScale = 1.0;    //cost of discarding needed cards
    double caution = 0.4;      //(0.1-0.33)
    double frontweight = 0.7;
    int trials = args.length>0?Integer.parseInt(args[0]):20;
    try{
    PrintStream out = args.length>1? new PrintStream(args[1]):System.out;
    out.println("Case,Hints,Fuse,Know,Play,Caution,Front,Average");
    int count = 1;
    //for(hintScale = 0.4; hintScale<0.6; hintScale+=0.1){
    //for(fuseScale = 0.5; fuseScale<1.5; fuseScale+=0.3){
    for(knowScale = 0.9; knowScale<=1.0; knowScale+=0.1){
    //for(playScale = 0.5; playScale<1.6; playScale+=0.5){
    //for(caution = 0.3; caution<=0.4; caution+=0.1){
    for(frontweight = 1.0; frontweight<=1.0; frontweight+=0.1){
    double total = 0.0;
    for(int i  = 0; i<trials; i++){
      Agent[] agents = {new agents.ModelAgent(hintScale, fuseScale, knowScale, playScale, caution, frontweight),
        new agents.ModelAgent(hintScale, fuseScale, knowScale, playScale, caution, frontweight),
        new agents.ModelAgent(hintScale, fuseScale, knowScale, playScale, caution, frontweight)};
      Hanabi game= new Hanabi(agents);
      int result = game.play();
      total+=result;
    }
    out.print(count++ +"," +hintScale);
    out.print(","+fuseScale);
    out.print(","+knowScale);
    out.print(","+playScale);
    out.print(","+caution);
    out.print(","+frontweight);
    out.println(","+total/trials);
    }
    //}
    //}
    }
    //}
    //}
    }
    catch(Exception e){System.out.println("IO Error");e.printStackTrace();}
    //StringBuffer log = new StringBuffer("A simple game for three basic agents:\n");
    //log.append("The final score is "+result+".\n");
    //log.append(critique(result));
    //System.out.print(log);
  }

}


