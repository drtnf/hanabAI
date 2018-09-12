# HanabAI
AI platform for the card game Hanabi.

# Description
This projects aims to provide a set of java classes and interface to facilliate agents playing the card game: Hanabi.
This project is designed for use in the unit 
[CITS3001](http://teaching.csse.uwa.edu.au/units/CITS3001/) *Algorithms, Agents and Artificial Intelligence* 
at The University of Western Australia.
Students may use and modify these files to reserach different artificial intelligence agents in the game Hanabi.
There will also be an AI bot tournament with the additional rules specified below. 

# Rules

## Rules - Hanabi

### Aim:
Hanabi is a cooperative game for 2 to 5 players, that is, 
a game where the players do not play against each other but work together towards a common goal.  
In this case they are absent minded firework manufacturers who accidentally mixed up powders, fuses and rockets from a firework display.
The show is about to start and panic is setting in.  They have to work together to stop the show becoming a disaster!  
The pyrotechnicians have to put together 5 fireworks, 1 white, 1 red,  1 blue, 1 yellow, 1 green), 
by making series rising in number (1, 2, 3, 4, 5) with the same coloured cards.

### Set up:
Place the 8 blue tokens in the lid of the box and the 3 red tokens just next to it.  
Shuffle the 50 cards to make a deck and put them face down.  Deal a hand of cards to each player:
If there are 2 or 3 players, each player receives 5 cards.  
If there are 4 or 5 players, each player receives 4 cards.

**Important** :*The players should not look at the cards which are dealt to them!  
They pick them up so that the other players can see them but so that they cannot see them themselves 
(so back to front!)  
They are not allowed to look at their own cards at all during the game.  
This would dishonour them and taint their reputation as master pyrotechnicians!*

### Play:
The players then take their turn going in a clockwise direction.  
On their turn, a player must complete one, and only one, of the following three actions (you are not allowed to skip your turn):
1. **Give one piece of information**:
  In order to carry out this task, there must be a blue token in the box lid:
  The player takes a blue token from the lid of the box and puts it at the side with the red tokens.
  They can then tell a teammate something about the cards that this player has in their hand.
  The player can either:
  - Give information about a single colour. 
    The player says "these cards are of colour X" and points to *every* X coloured card in the players hand.
    The player cannot say another payer has no cards of some colour.
  - Give information about a single number.
    The player says "these cards have number Y" and points to *every* Y numbered card in the players hand.
    The player cannot say another payer has no cards of some number.
2. **Discard a card**:
  Performing this task allows a blue token to be returned to the lid of the box. 
  The player discards a card from his hand and puts it in the discard pile (next to the box, face up). 
  They then takes a new card and adds it to their hand without looking at it.
3. **Play a card**:
  The player takes a card from their hand and puts it face up on the table.
  Two options are possible:
  - The card either begins or completes a firework and it is then added to this firework
  - Or the card does not complete any firework it is then discarded and a red token is added to the lid of the box
  The player then takes a new card and adds it to his hand without looking at it

There can only be one firework of each colour. 
The cards for a firework have to be placed in rising order (1, then 2, then 3, then 4 and finally 5).
There can only be one card of each value in each firework (so 5 cards in total).

When a player completes a firework i.e. plays the card with a value of 5,
the player puts a blue token back in the lid of the box.  

### End of game:
There are 3 ways to end the game of Hanabi :
1. The game ends immediately and is lost if the third red token is placed in the lid of the box (everyone scores 0 points).
2. The game ends immediately and it is a stunning victory if 
  the firework makers manage to make the 5 fireworks before the cards run out.
  The players are then awarded the maximum score of 25 points.
3. The game ends if a firework maker takes the last card from the pile.  
  each player plays one more time, including the player who picked up the last card

In order to calculate their scores, the players add up the largest value card for each of
the 5 fireworks. Artistic impression is determined by the Firework Manufacturers International Federation reference scale:

| **Points** | **Overall Impression**                                    |
|:----------:|-----------------------------------------------------------|
| 0-5        | horrible, booed by the crowd.                             |
| 6-10       | mediocre, just a spattering of applause.                  |
| 11-15      | honourable, but will not be remembered for very long...   |
| 16-20      | excellent, crowd pleasing.                                |                                  
| 21 - 24    | amazing, will be remembered for a very long time!         |
| 25         | legendary, everyone left speechless, stars in their eyes  |


## Rules - Tournament

1. AI bots must implement the provided agent interface, and there are restrictions for the amount of computation, 
  and system resources they can use: Each agent has 1 second for their turn.
3. Agents may not use threading to access any additional computational resources.
4. Agents may not read or write from the file system. Agents should not print to the screen either.
5. Agents should have a parameterless constructor.
5. Each student may submit one agent class called Agent12345678.java, where the digits are your student number.
6. The tournament will be a selection of random games, 
  where your agent will be grouped with randomly chosen agents from other students. Game.java has a sample tournament.
7. Agents will be randomly selected to play in games of 5 players.
8. At the end of the game, each agent receives the number of points scored.
9. Agents will be ranked on the average score of the games they participate in. 
10. Any attempt to cheat will result in immediate disqualification.
11. Any Agent who crashes, or gets stuck in an infinite loop will be removed from the tournament.
12. Agents who go over time, will be penalised or removed from the competition.

Good luck.

## Compiling and Running:
- Your agent class should be in the package agents, and import the package hanabAI.
- To compile use your favorite IDE, or from the command line, use:
 `javac -d bin src/*/*.java`
- To run a game, you can edit the main method in hanabAI.Hanabi to use your agents, rather than agents.BasicAgent.
However, it is better to build a contest class in the default package to run extensive experiments.
- To run the main method in hanabAI.Hanabi, use your favorite IDE, or from the commandline, use:
 `java -cp bin hanabAI.Hanabi`
