package hanabAI;
import java.util.*;
/**
 * Represent the game state in Hanabi.
 * The state is designed as an immutable object.
 * Agents are able to get the current game state, 
 * and all previous moves using a state object.
 * @author Tim French
 **/

public class State implements Cloneable{
  

  /**The name of each of the players in the game**/
  private String[] players;
  /**The stack of cards that have bee discarded, or incorrectly played**/
  private Stack<Card> discards;
  /**For each colour, the cards making up that firework so far**/
  private Map<Colour,Stack<Card>> fireworks;
  /**The hand of each player**/
  private Card[][] hands;
  /**The order of this state in the game**/
  private int order=0;
  /**The number of hints remaining**/
  private int hints=0;
  /**The number of fuse tokens left**/
  private int fuse=0;
  /**The observer of this state. This allows hidden information to be redacted**/
  private int observer=-1;
  /**The previous State of the game, so that all states are accessible back to the first state (with a null previous state)**/
  private State previousState;
  /**A list of all moves made so far in the game, in the order they were played**/
  private Action previousAction;
  /**The index of the next player to move**/
  private int nextPlayer=-1;
  /**The fnal play of the game (for when the deck runs out)**/
  private int finalAction=-1;

  /**A constructor for the first state in the game
   * @param players the names of the players in the game, in an array by index
   * @param deck the shuffled deck of cards to be used for the deal
   * @throws IllegalArgumentException if arguments are null, or the wrong size**/
  public State(String[] players, Stack<Card> deck) throws IllegalArgumentException{
    if(players==null || players.length<2 || players.length >5 || deck == null || deck.size() !=50)
      throw new IllegalArgumentException("incorrect parameters");
    this.players = players.clone();
    discards = new Stack<Card>();
    fireworks = new HashMap<Colour,Stack<Card>>();
    for(Colour c: Colour.values())fireworks.put(c,new Stack<Card>());
    hands = new Card[players.length][players.length>3?4:5];
    for(int i = 0; i<hands.length; i++)
      for(int j = 0; j<hands[i].length; j++) 
        hands[i][j]=deck.pop();
    order = 0;
    hints = 8;
    fuse = 3;
    observer = -1;  
    nextPlayer = 0;
    finalAction = -1;
  }

  /**
   *A method to create the next state from the given state and a move.
   *This method will only work if no hand has been hidden (since it allows agents to infer the result of actions).
   *@param deck the deck of cards
   *@param action the action made
   *@throws IllegalActionException if the move is not legal in the current state, or one hand has been hidden.
   **/
  public State nextState(Action action, Stack<Card> deck) throws IllegalActionException{
   if(!legalAction(action)) throw new IllegalActionException("Invalid action!: "+action);
   if(gameOver()) throw new IllegalActionException("Game Over!");
   if(observer!=-1) throw new IllegalActionException("Next state unavailable!");
   State s = (State)this.clone();
   switch(action.getType()){
     case PLAY:
       Card c = hands[action.getPlayer()][action.getCard()];
       Stack<Card> fw = fireworks.get(c.getColour());
       if((fw.isEmpty() && c.getValue() == 1) || (!fw.isEmpty() && fw.peek().getValue()==c.getValue()-1)){
         s.fireworks.get(c.getColour()).push(c);
         if(s.fireworks.get(c.getColour()).size()==5 && s.hints<8) s.hints++;
       }
       else{
         s.discards.push(c);
         s.fuse--;
       }
       if(!deck.isEmpty()) s.hands[action.getPlayer()][action.getCard()] = deck.pop();
       else{
        if(finalAction==-1) s.finalAction = order+players.length;
        s.hands[action.getPlayer()][action.getCard()] = null;
       }
       break;  
     case DISCARD:
       c = hands[action.getPlayer()][action.getCard()];
       s.discards.push(c);
       if(!deck.isEmpty()) s.hands[action.getPlayer()][action.getCard()] = deck.pop();
       else{
        if(finalAction==-1) s.finalAction = order+players.length;
        s.hands[action.getPlayer()][action.getCard()] = null;
       }
       if(hints<8) s.hints++;
       break;
     case HINT_COLOUR: 
       s.hints--; 
       break;
     case HINT_VALUE: 
       s.hints--;
       break;
     default: break;
   }
   s.order++;
   s.previousAction = action;
   s.nextPlayer = (nextPlayer+1)%players.length; 
   s.previousState = this;  
   return s;
  }

  /**
   *A method to create a local state from a global state. 
   *That is, if there is no current observer, the specified observer will have their hand hidden from them.
   *@param observer the player observing the game state
   *@throws IllegalActionException if the observer is out of bounds, or if the state is not global.
   **/
  public State hideHand(int observer) throws IllegalActionException{
    if(this.observer==-1 && observer>=0 && observer < hands.length){
      State local = (State) this.clone();
      local.observer=observer;
      return local;
    }
    else throw new IllegalActionException("Hand already hidden, or observer out of bounds");
  }
  
  /**
   * Test the legality of a Action.
   * If the observer of a state is specified, this mathod can only be applied to actions performed by the observer. 
   * @param a the move to be tested
   * @return true if the move is legal in the current game state.
   * @throws IllegalActionException
   **/
  public boolean legalAction(Action a) throws IllegalActionException{
    if(observer!=-1 && a.getPlayer()!=observer) throw new IllegalActionException("Local states may only test the legality of observers moves");
    if(a.getPlayer()!=nextPlayer) return false;
    switch(a.getType()){
      case PLAY:
        return (a.getCard()>=0 && a.getCard()<hands[nextPlayer].length); 
      case DISCARD: 
        if(hints==8) throw new IllegalActionException("Discards cannot be made when there are 8 hint tokens");
        return (a.getCard()>=0 && a.getCard()<hands[nextPlayer].length);
      case HINT_COLOUR:
        if(hints==0 || a.getHintReceiver() <0 || a.getHintReceiver()>players.length || a.getHintReceiver() == a.getPlayer()) return false; 
        boolean[] hint = new boolean[hands[a.getHintReceiver()].length];
        for(int i = 0; i<hint.length; i++){
          Card c = hands[a.getHintReceiver()][i];
          hint[i] = (c==null?null:c.getColour())==a.getColour();
        }
        return Arrays.equals(hint, a.getHintedCards());
      case HINT_VALUE:
        if(hints==0 || a.getHintReceiver() <0 || a.getHintReceiver()>players.length || a.getHintReceiver() == a.getPlayer()) return false; 
        hint = new boolean[hands[a.getHintReceiver()].length];
        for(int i = 0; i<hint.length; i++){
          Card c = hands[a.getHintReceiver()][i];
          hint[i] = (c==null?-1:c.getValue())==a.getValue();
        }
        return Arrays.equals(hint, a.getHintedCards());
      default: return false;
    }
  }
  
  /**
   * Gives and array of all the player names in the game.
   * @return an array containing the naems of the players in the game, by ther index in the game.
   **/
  public String[] getPlayers(){
   return players.clone();
  } 
  
  /**
   * Gives the cards of the specified player
   * @param player the index of the player is the game
   * @return an array of cards in player's hand, or an empty array, if the cards are hidden.
   * @throws ArrayIndexOutOfBounds if there is no player of the given index.
   **/  
  public Card[] getHand(int player)throws ArrayIndexOutOfBoundsException{
    if(player<0 || player>=players.length) throw new ArrayIndexOutOfBoundsException();
    if(player==observer) return new Card[hands[player].length];
    return hands[player].clone();
  }

  /**
   * Gives a players name
   * @return the name of the specified player
   **/
  public String getName(int player){return players[player];}

  /**
   * Gives the previous state of the game, allowing agents to determine the recent actions in the game.
   * @return the previous state, with the same observer as the current state.
   **/
  public State getPreviousState(){
    State s = previousState;
    s.observer = observer;
    return s;
  }

  /**
   * Gets the last action performed in the game, before this state was reached
   * @return the last action performed prior to this state.
   **/
  public Action getPreviousAction(){return previousAction;}


  /**
   * Gets the last action performed in the game, by the specified player
   * @return the last action performed by the given player, prior to this state.
   * @throws ArrayIndexOUtOfBoundsException if the specified player has not yet performed an action
   **/
  public Action getPreviousAction(int player){
    State s = this;
    while(s!=null && s.previousAction !=null && s.previousAction.getPlayer()!=player)
      s = s.previousState;
    if(s==null || s.previousAction==null) throw new ArrayIndexOutOfBoundsException("Player has not played yet");
    else return s.previousAction;
  }

  /**
   * Gets the card played in the previous move, 
   * or null if it is the first move, 
   * or the previous move was a hint.
   * @return the card played in the previous action, 
   * or null if there is no previous action, or the action was a hint.
   * */
  public Card previousCardPlayed(){
    try{
      return previousState.hands[previousAction.getPlayer()][previousAction.getCard()];
    }
    catch(Exception e){return null;}
  }


  /** 
   * Gets a clone of the discard stack
   * @return a clone of the discard stack
   **/
  public Stack<Card> getDiscards(){return (Stack<Card>) discards.clone();}

  /**
   * Get the stack of cards representing the specified firework
   * @return a clone of the stack of cards representing the firework of the given colour. The highest card is at the top of the stack.
   **/
  public Stack<Card> getFirework(Colour c){return (Stack<Card>) fireworks.get(c).clone();}

  /**
   * Get the number of hint tokens available
   * @return number of hints remaining
   **/

  public int getHintTokens(){return hints;}

  /**
   * Get the number of fuse tokens available
   * @return number of fuse tokens remaining
   **/
  public int getFuseTokens(){return fuse;}



  /**
   * Gets the observer, or -1 if global state
   * @return the agent index of the observer, or -1 if that state is Global
   **/
  public int getObserver(){return observer;}


  /**
   * Gets the nextplayer, or -1 if gameOver
   * @return the agent index of the nextplayer, or -1 if the game is over.
   **/
  public int getNextPlayer(){return (gameOver()?-1:nextPlayer);}
  
  /**
   * Gets the order of the state, starting from 1.
   * @return the order the state appears in the game, from first (1) to last.
   **/
  public int getOrder(){return order;}

  /**
   * Returns the order of the final action, if it is known.
   * When the final card is drawn from the deck, every player has one more action remaining.
   * The order of the final action, is the order of the action that drew the final card, 
   * plus the number of players in the game.
   * @return the order of the final action, or -1 if the deck is not empty
   **/
  public int getFinalActionIndex(){return finalAction;} 

  /**
   * Get the current score
   * @return the sum of the highest value cards in each firework
   **/
  public int getScore(){
    int score = 0;
    if(fuse==0) return 0;
    for(Colour c: Colour.values()) 
      if(!fireworks.get(c).isEmpty())score+=fireworks.get(c).peek().getValue();
    return score;
  }

  /**
   * Tests if the game is over
   * @return true if all fireworks have been made, the deck has run out, or a fues has exploded.
   **/
  public boolean gameOver(){
    return (order==finalAction || fuse == 0 || getScore()==25);
  }

  /**
   * Produces a clone of the state
   **/
  public Object clone(){
    try{
      State s = (State) super.clone();
      s.players = players.clone();
      s.discards = (Stack<Card>)discards.clone();
      s.hands = (Card[][]) hands.clone();
      for(int i = 0; i<hands.length; i++) s.hands[i] = (Card[])s.hands[i].clone();
      s.fireworks = (Map<Colour,Stack<Card>>)((HashMap)fireworks).clone();
      for(Colour c: Colour.values()) s.fireworks.put(c,(Stack<Card>)fireworks.get(c).clone());
      return s;
    }
    catch(CloneNotSupportedException e){return null;}
  }

  /**
   * Returns a string describing the state of the game, including: 
   * the state of each players hand;
   * the top card of each fire work;
   * and the last action.
   * */
  public String toString(){
    String ret = "State: "+order+"\n";
    ret+="Last move: "+previousAction+"\n";
    ret+="Players' hands:\n";
    for(int i = 0; i<players.length; i++){
      ret+=players[i]+" ("+i+"): ";
      for(Card c: hands[i])ret+=c+" ";
      ret+="\n";
    }
    ret+="Fireworks:\n";
    for(Colour c: Colour.values())
      ret+=c+"  "+(fireworks.get(c).isEmpty()? "-" : fireworks.get(c).peek()) +"\n";
    ret+= "Hints: "+hints+"\nFuse: "+fuse+"\n";
    return ret;
  }

}



