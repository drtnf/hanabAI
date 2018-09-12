package hanabAI;

/**
 * An abstract class to represent actions in the game Hanabi.
 * The class is designed to be immutable.
 * @author Tim French
 **/
public class Action{
  //the player performing the action
  private int player;
  //the player's name (for nice formatting)
  private String playerName;
  //The type of action
  private ActionType type;
  //The position of the card played/discarded
  private int card;
  //The player recieving the hint, if the action is a hint
  private int hintee;
  //The cards hinted at, if the action is a hint
  private boolean[] cards;
  //The Colour hinted, if the action is a colour hint
  private Colour colour;
  //The value hinted, if the action is a value hint.
  private int value;

  //common constructor for all actions
  private Action(int player, String playerName, ActionType type){
    this.player = player;
    this.playerName = playerName;
    this.type = type;
  }

  /**Constructor to create Play or Discard actions
   * @param player the index of the player performing the action
   * @param playerName the naem of the player performing the action
   * @param type the type of the action, should be ActionType.PLAY or actionType.DISCARD
   * @param pos the position of the card to be discarded in the players hand
   * @throws IllegalActionException if the wrong ActionType is given
   * */
  public Action(int player, String playerName, ActionType type, int pos) throws IllegalActionException{
    this(player, playerName, type);
    if(type != ActionType.PLAY && type!= ActionType.DISCARD) throw new IllegalActionException("Wrong parameters for action type");
    card = pos;
  }

  /**Constructor to create Play or Discard actions
   * @param player the index of the player performing the action
   * @param playerName the naem of the player performing the action
   * @param type the type of the action, must be ActionType.HINT_COLOUR
   * @param hintReceiver the index of the player recieving the hint
   * @param cards an array of booleans, such that the ith value is true if and only if the ith card in the hintee's hand matches the hint
   * @param hint the colour hinted at.
   * @throws IllegalActionException if the wrong ActionType is given
   * */
  public Action(int player, String playerName, ActionType type, int hintReceiver, boolean[] cards, Colour hint) throws IllegalActionException{
    this(player, playerName, type);
    if(type != ActionType.HINT_COLOUR) throw new IllegalActionException("Wrong parameters for action type");
    this.hintee = hintReceiver;
    this.cards = cards;
    this.colour = hint;
  }
  
  /**Constructor to create Play or Discard actions
   * @param player the index of the player performing the action
   * @param playerName the naem of the player performing the action
   * @param type the type of the action, must be ActionType.HINT_VALUE
   * @param hintReceiver the index of the player recieving the hint
   * @param cards an array of booleans, such that the ith value is true if and only if the ith card in the hintee's hand matches the hint
   * @param hint the value hinted at.
   * @throws IllegalActionException if the wrong ActionType is given
   * */
  public Action(int player, String playerName, ActionType type, int hintReceiver, boolean[] cards, int hint) throws IllegalActionException{
    this(player, playerName, type);
    if(type != ActionType.HINT_VALUE) throw new IllegalActionException("Wrong parameters for action type");
    this.hintee = hintReceiver;
    this.cards = cards;
    this.value = hint;
  }

  /**
   * get the player index
   * @return the index of the player performing the action
   **/ 
  public int getPlayer(){return player;}

  /**
   * get the action type
   * @return the type of action being performed 
   **/ 
  public ActionType getType(){return type;}

  /**
   * gets the psoition of the card being played/discarded
   * @return the position of the card being played or discarded
   * @throws IllegalActionException if the action type is not PLAY or DISCARD
   **/ 
  public int getCard() throws IllegalActionException{
    if(type != ActionType.PLAY && type!= ActionType.DISCARD) throw new IllegalActionException("Card is not defined");
    return card;
  }


  /**
   * gets the index ofthe player receiving the hint
   * @return the index of the player receiving the hint
   * @throws IllegalActionException if the action type is not HINT_COLOUR or HINT_VALUE
   **/ 
  public int getHintReceiver() throws IllegalActionException{
    if(type != ActionType.HINT_COLOUR && type!= ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a hint");
    return hintee;
  }

  /**
   * gets an array of booleans indicating the cards that are the subject of the hint
   * @return an array of booleans, such that the ith valeu is true if and only if the ith card in the hintReceivers hand matches the hint (indexing from 0)
   * @throws IllegalActionException if the action type is not HINT_COLOUR or HINT_VALUE
   **/ 
  public boolean[] getHintedCards() throws IllegalActionException{
    if(type != ActionType.HINT_COLOUR && type!= ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a hint");
    return cards.clone();
  }

  /**
   * gets the colour hinted
   * @return the colour hinted
   * @throws IllegalActionException if the action type is not HINT_COLOUR
   **/ 
  public Colour getColour() throws IllegalActionException{
    if(type != ActionType.HINT_COLOUR) throw new IllegalActionException("Action is not a colour hint");
    return colour;
  }

  /**
   * gets the value hinted
   * @return the value hinted
   * @throws IllegalActionException if the action type is not HINT_VALUE
   **/ 
  public int getValue() throws IllegalActionException{
    if(type != ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a value hint");
    return value;
  }

  /**
   * A string representation of the action being performed
   * @return a description of the action, depending on type
   * */
  public String toString(){
    String ret;
    switch(type){
      case PLAY: return "Player "+playerName+ "("+player+") plays the card at position "+card;
      case DISCARD: return "Player "+playerName+ "("+player+") discards the card at position "+card;
      case HINT_COLOUR: 
       ret = "Player "+playerName+ "("+player+") gives the hint: \"Player "+hintee+", cards at position"+(cards.length>1?"s":"");
       for(int i=0; i<cards.length; i++)ret += (cards[i]?" "+i:"");
       ret+= " have colour "+colour+"\"";
       return ret;
      case HINT_VALUE: 
       ret = "Player "+playerName+ "("+player+") gives the hint: \"Player "+hintee+", cards at position"+(cards.length>1?"s":"");
       for(int i=0; i<cards.length; i++)ret += (cards[i]?" "+i:"");
       ret+= " have value "+value+"\"";
       return ret;
    }
    return "";
  }
}
