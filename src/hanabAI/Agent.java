package hanabAI;

/**
 * An interface for representing the strategy of a player in Hanabi
 * */
public interface Agent{

  /**
   * Reports the agents name
   * */
  public String toString();

  /**
   * Given the state, return the action that the strategy chooses for this state.
   * @return the action the agent chooses to perform
   * */
  public Action doAction(State s);

}


