package hanabAI;

import java.util.Stack;

/**An immutable class for representing Hanabi cards**/
public class Card{
  private Colour colour;//the card's colour
  private int value;//the number on the card

  /**
   * Constrcuts a card with the specified colour and value
   * @throws IllegalArgumentException if the Value is not between 1 and 5 inclusive
   **/
  public Card(Colour c, int val) throws IllegalArgumentException{
    if(val<1 || val>5) throw new IllegalArgumentException("Card value out of range");
    colour = c;
    value = val;
  }

  /**
   *Get the colour
   *@return the colour of the card
   */
  public Colour getColour(){return colour;}

  /**
   *Get the value
   *@return the value of the card
   */
  public int getValue(){return value;}

  /**
   *Get the numerosity of the card
   *@return the number of times the card appears in the deck
   */
  public int getCount(){return (value==1?3:(value<5?2:1));}

  /**
   *Give a String representation of the card
   *@return the strinf representation of the card
   */
  public String toString(){return colour.toString()+"-"+value;}

  /**
   * Gives a new instance of a complete deck of cards, ordered by colour and value
   * @return an array of cards corresponding to a standard Hanabi deck 
   **/
  public static Card[] getDeck(){return deck.clone();}

  /**
   * Gives a new instance of a shuffled deck of cards deck of cards.
   * Pairs of random cards are swapped 100 times, and placed into a stack. 
   * @return a stack of Hanabi cards in random order 
   **/
  public static Stack<Card> shuffledDeck(){
    Card[] deck = getDeck();
    java.util.Random r = new java.util.Random();
    for(int i = 0; i<1000; i++){
      int a = r.nextInt(50);
      int b = r.nextInt(50);
      Card c = deck[a];
      deck[a]= deck[b];
      deck[b]=c;
    }
    Stack<Card> shuffle = new Stack<Card>();
    for(Card c: deck) shuffle.push(c);
    return shuffle;
  }

  /**
   * Cards are compared by colour and value.
   **/ 
  public boolean equals(Object o){
    if(o!=null && o instanceof Card){ 
      Card c = (Card)o;
      return c.colour == colour && c.value==value;
    }
    return false;
  }

  public int hashCode(){
    return 5*colour.ordinal()+value;
  }


  private static Card[] deck = {
    new Card(Colour.BLUE,1),new Card(Colour.BLUE,1), new Card(Colour.BLUE,1),
    new Card(Colour.BLUE,2),new Card(Colour.BLUE,2),new Card(Colour.BLUE,3),new Card(Colour.BLUE,3),
    new Card(Colour.BLUE,4),new Card(Colour.BLUE,4),new Card(Colour.BLUE,5),
    new Card(Colour.RED,1),new Card(Colour.RED,1), new Card(Colour.RED,1),
    new Card(Colour.RED,2),new Card(Colour.RED,2),new Card(Colour.RED,3),new Card(Colour.RED,3),
    new Card(Colour.RED,4),new Card(Colour.RED,4),new Card(Colour.RED,5),
    new Card(Colour.GREEN,1),new Card(Colour.GREEN,1), new Card(Colour.GREEN,1),
    new Card(Colour.GREEN,2),new Card(Colour.GREEN,2),new Card(Colour.GREEN,3),new Card(Colour.GREEN,3),
    new Card(Colour.GREEN,4),new Card(Colour.GREEN,4),new Card(Colour.GREEN,5),
    new Card(Colour.WHITE,1),new Card(Colour.WHITE,1), new Card(Colour.WHITE,1),
    new Card(Colour.WHITE,2),new Card(Colour.WHITE,2),new Card(Colour.WHITE,3),new Card(Colour.WHITE,3),
    new Card(Colour.WHITE,4),new Card(Colour.WHITE,4),new Card(Colour.WHITE,5),
    new Card(Colour.YELLOW,1),new Card(Colour.YELLOW,1), new Card(Colour.YELLOW,1),
    new Card(Colour.YELLOW,2),new Card(Colour.YELLOW,2),new Card(Colour.YELLOW,3),new Card(Colour.YELLOW,3),
    new Card(Colour.YELLOW,4),new Card(Colour.YELLOW,4),new Card(Colour.YELLOW,5)
  };

}


