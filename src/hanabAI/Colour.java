package hanabAI;

/**The colours of Hanabi Cards**/
public enum Colour{
  BLUE,RED,GREEN,WHITE,YELLOW;

  /**@return String representation of the colour**/
  public String toString(){
    switch(this){
      case BLUE: return "Blue";
      case RED: return"Red";
      case GREEN: return "Green";
      case WHITE: return "White";
      case YELLOW: return "Yellow";
    }
    return "";
  }
} 


