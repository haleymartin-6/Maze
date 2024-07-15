# Maze
A generate maze with keyboard controls to navigate. 

## MazeGame Instructions:


Welcome to our maze game! This game has many features that allow for you to have a one of a kind experience that will make solving a maze fun and enjoyable!


First off, we want to start off by telling you about the animation feature; our maze is constructed one piece at a time and is slowly animated to display to you the final maze. Now this animation process typically takes due time, but with larger maze sizes the animation may take a minute or so to fully construct. IMPORTANT: we have made it so that you are unable to click any keys until the maze is fully constructed, so just wait until the maze has stopped animating to start pressing the keys!


Onto our on-key attributes. We have quite a few so I will list them out:

* Press the “r” key to generate a new random maze (this will restart the construction animation so be patient!!)
* Press the “b” key after the animation to construct the game has completed in order to conduct the breadth first search
* Press the “d” key after the animation to construct the game has completed in order to conduct the depth first search
* Press the “k” key if you wish to pause the search animation during the animation of one of the search algorithms 
* Press the up arrow key to solve the game manually and move the cell up again after the animation to construct the game has completed 
* Press the down arrow key to solve the game manually and move the cell down again after the animation to construct the game has completed 
* Press the left arrow key to solve the game manually and move the cell to the left again after the animation to construct the game has completed  
* Press the right arrow key to solve the game manually and move the cell to the right again after the animation to construct the game has completed  
* Press the “v” key to get rid of the cyan color of the visited cells that were visited during the search algorithm or if you want to get rid of the color which you are manually searching the maze with the arrow keys


Alright, and after that we will move on to tell you about how to change the sizing of the maze and the size of the cells in the maze. If you navigate to the MazeGameWorld class, you will see at the top there are values of height, width, and scale. They should already be predefined as follows:


Int width = 50;
Int height = 40;
Int scale = 10;


You are able to change those values of the width and height to manipulate the size of the maze. CATION: When testing the maze of size 100 X 60 keep the scale at 10 or else the maze will go off the display of the laptop. The scale is used to change the size of the cells: a higher scale means bigger cells and the opposite goes for a smaller scale. 


Hope you enjoy it :)
