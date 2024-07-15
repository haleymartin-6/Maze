import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents the cell class which are what makes up the path of the maze 
class Cell {
  int row;
  int col;

  int id;

  boolean wasVisited;
  boolean isInSolvePath;

  ArrayList<Cell> adj;

  ArrayList<Boolean> walls;

  Cell(int row, int col, int id) {
    if (row < 0 || col < 0 || id < 0) {
      throw new IllegalArgumentException("your row col or id cannot be negative");
    }
    this.row = row;
    this.col = col;

    this.id = id;

    wasVisited = false;
    isInSolvePath = false;

    adj = new ArrayList<Cell>();
    for (int i = 0; i < 4; i++) {
      adj.add(null);
    }

    walls = new ArrayList<Boolean>();
    for (int i = 0; i < 4; i++) {
      walls.add(true);
    }
  }

  // checks if the from cell in this edge has the same row or colum and then
  // delegates the actual changing of the cells fields to the cell class
  void checkCellEqual(Cell to, ArrayList<ArrayList<Cell>> maze) {
    if (this.col == to.col) {
      maze.get(this.row).get(this.col).walls.set(1, false);
      maze.get(to.row).get(to.col).walls.set(0, false);
    }

    if (this.row == to.row) {
      maze.get(this.row).get(this.col).walls.set(3, false);
      maze.get(to.row).get(to.col).walls.set(2, false);
    }
  }

  // helper function to find the root of the list of links to link up the cells id
  int findRootHelp(ArrayList<Integer> links) {
    return this.findRoot(links, this.id);
  }

  int findRoot(ArrayList<Integer> links, int ids) {
    int link = links.get(ids);

    // found root
    if (link == ids) {
      return ids;
    }

    return findRoot(links, link);
  }

  // put a cell in the adj list
  // EFFECT: sets the a cell at a given index in the adj list of this cell
  void setAdj(int index, Cell given) {
    this.adj.set(index, given);
  }

  // put a boolean in the walls list
  // EFFECT: sets the boolean at a given index in the walls list of this cell
  void setWalls(int index, boolean given) {
    this.walls.set(index, given);
  }

  // accesses the walls at a given index
  public boolean acessAtWalls(int i) {
    return this.walls.get(i);
  }

  // accesses the adj list at a given index
  public Cell acessAtAdj(int i) {
    return this.adj.get(i);
  }

  // sets a given cells solved path to true
  // EFFECT: the isInSolvePath field of the cell is set to true
  void cellPathTrue() {
    this.isInSolvePath = true;
  }

  // sets a given cells visited field to true
  // EFFECT: the wasVisited field of the cell is set to true
  void setWasVisted() {
    this.wasVisited = true;
  }

  // checks if the given cells row and col are equal to the supplied values
  boolean checkBothCell(int max, int min) {
    return this.row == max && this.col == min;
  }

  // returns the edge from the given path at the index of this cells id
  Edge makeEdge(HashMap<Integer, Edge> path) {
    return path.get(this.id);
  }

  // returns the value of this cell wasVisited field
  boolean findWasVisit() {
    return this.wasVisited;
  }

  // checks if this cells row field is equal to the height minus 1 and if
  // this cells col field is equal to the width minus 1
  boolean checkIfOver(int height, int width) {
    return this.row == height - 1 && this.col == width - 1;
  }

  // puts a given edge at a given index of this cells id
  // EFFECT: the hashMap path that is passed in now has a new edge at the index
  // of this cells id
  void changePath(HashMap<Integer, Edge> path, Edge edge) {
    path.put(this.id, edge);
  }

  // returns the value of this cells isInSolvePath field
  boolean findSolvePath() {
    return this.isInSolvePath;
  }
}

// represents the edges of the cells in the maze
class Edge {
  Cell from;
  Cell to;
  int weight;

  Edge(Cell from, Cell to, int weight) {
    if (weight < 0) {
      throw new IllegalArgumentException("weight cannot be negative");
    }
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // checks if the from cell in this edge has the same row or colum and then
  // delegates the actual changing of the cells fields to the cell class
  void checkEdgeEqual(ArrayList<ArrayList<Cell>> maze) {
    this.from.checkCellEqual(this.to, maze);
  }

  // finds the root of the to cell where the id is the same index as the link
  // and delegates the method and recursive calls to the cell class
  // EFFECT: changes the to cell fields to have their fields lists be set
  // to a given value based off the conditions stated in the helper method
  int findRootTo(ArrayList<Integer> links) {
    return this.to.findRootHelp(links);
  }

  // finds the root of the from cell where the id is the same index as the link
  // and delegates the method and recursive calls to the cell class
  // EFFECT: changes the from cell fields to have their fields lists be set
  // to a given value based off the conditions stated in the helper method
  int findRootFrom(ArrayList<Integer> links) {
    return this.from.findRootHelp(links);
  }

  // finds and returns the from cell in this edge
  Cell findFrom() {
    return this.from;
  }

}

//class that holds the compare method to compare two edges
class Utils implements Comparator<Edge> {

  // compares two edges by weight, returns negative if o2s weight is greater than
  // o1s,
  // returns positive if o1s weight is greater than o2s weight, and zero if they
  // are equal
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

// represents the entire maze game
class MazeGameWorld extends World {

  // static Random random = new Random();
  int width = 50;
  int height = 40;
  int scale = 10;
  Random rand = new Random();

  ArrayList<ArrayList<Cell>> maze;

  ArrayList<Edge> tree;
  int posInTree;
  boolean isFinishedBuildingMaze;

  boolean depthFirst = true;
  HashMap<Integer, Edge> path;
  ArrayList<Cell> worklist;

  int stepsTotal = 0;
  int stepsWrong = 0;
  int stepsToSolve = 0;

  boolean isWon;
  boolean startTimer;
  boolean manualSolve = false;
  boolean showVisited = true;

  double time = 0.0;

  Cell cur;

  boolean aiSolvingMaze = false;
  int speed = 10;

  MazeGameWorld(int width, int height, int scale, boolean testing) {
    if (width <= 0 || height <= 0 || scale <= 0) {
      throw new IllegalArgumentException("You cannot have a negative or zero sized maze :(");
    }
    if (scale > width || scale > height) {
      throw new IllegalArgumentException("Scale cannot be greater than your width");
    }

    if (testing) {
      this.rand = new Random(1);
    }
    else {
      this.rand = new Random();
    }
    this.width = width;
    this.height = height;
    this.scale = scale;
    makeMaze();
  }

  MazeGameWorld() {
    makeMaze();
    bigBang(this.width * this.scale + 2, this.height * this.scale + 2 + 100, 0.0000000111);
  }

  // generates the maze
  // EFFECT: sets cells adj and walls list to include newly generated cells as
  // well
  void makeMaze() {

    this.posInTree = 0;
    this.isFinishedBuildingMaze = false;
    this.time = 0;
    this.stepsTotal = 0;
    this.stepsToSolve = 0;
    this.stepsWrong = 0;

    this.maze = new ArrayList<>();

    for (int row = 0; row < this.height; row++) {

      this.maze.add(new ArrayList<>());

      for (int col = 0; col < this.width; col++) {

        Cell cell = new Cell(row, col, row * this.width + col);
        this.maze.get(row).add(cell);

      }
    }

    ArrayList<Edge> edges = new ArrayList<>();

    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {

        // for cells located on the edges
        Cell cell = this.maze.get(row).get(col);

        if (row != 0) {
          // top
          cell.setAdj(0, this.maze.get(row - 1).get(col));
        }
        else {
          cell.setWalls(0, true);
        }
        if (row != this.height - 1) {
          // bottom
          cell.setAdj(1, this.maze.get(row + 1).get(col));

          edges.add(new Edge(cell, cell.acessAtAdj(1), Math.abs(rand.nextInt())));
        }
        else {
          cell.setWalls(1, true);
        }
        if (col != 0) {
          // left
          cell.setAdj(2, maze.get(row).get(col - 1));
        }
        else {
          cell.setWalls(2, true);
        }
        if (col != this.width - 1) {
          // right
          cell.setAdj(3, this.maze.get(row).get(col + 1));

          edges.add(new Edge(cell, cell.acessAtAdj(3), Math.abs(rand.nextInt())));
        }
        else {
          cell.setWalls(3, true);
        }

      }
    }

    Comparator<Edge> edgeCompare = new Utils();
    edges.sort(edgeCompare);

    // each node gets linked to itself
    ArrayList<Integer> links = new ArrayList<Integer>();

    for (int i = 0; i < edges.size(); i++) {
      links.add(i);
    }

    tree = new ArrayList<>();

    int roots = 0;

    while (roots != 1 && edges.size() > 0) {
      for (int i = 0; i < edges.size(); i++) {
        Edge e = edges.get(i);

        int repA = e.findRootFrom(links);
        int repB = e.findRootTo(links);

        if (repA == repB) {
          edges.remove(e);
          i--;
        }
        else {
          links.set(repA, repB);
          tree.add(e);
        }
      }

      roots = 0;
      for (int i = 0; i < links.size(); i++) {
        if (i == links.get(i)) {
          roots++;
          if (roots > 1) {
            break;
          }
        }
      }
    }

    this.cur = this.maze.get(0).get(0);
  }

  // removes the walls from the given maze
  // EFFECT: generates the maze with the walls knocked down to create
  // the maze path
  void removeMazeWall() {

    if (this.posInTree >= this.tree.size()) {
      this.isFinishedBuildingMaze = true;
      return;
    }

    Edge e = this.tree.get(this.posInTree);

    // removes a wall from the maze
    e.checkEdgeEqual(this.maze);

    this.posInTree++;
  }

  // reconstructs the maze
  // EFFECT: constructs and conunts the steps that it took to construct the maze
  void reconstruct(HashMap<Integer, Edge> path, Cell cell) {

    cell.cellPathTrue();
    this.stepsToSolve++;

    if (cell.checkBothCell(0, 0)) {
      this.stepsWrong = this.stepsTotal - this.stepsToSolve;
      return;
    }

    Edge edge = cell.makeEdge(path);
    Cell prev = edge.findFrom();
    reconstruct(path, prev);
  }

  // sets up the solver that is used to search the maze based on the given type of
  // search
  // algorithum inputed by the user
  // EFFECT: changes the fields of the Maze to be set up for the provided search
  // algo that
  // the user inputed
  void setupMazeSolver(String algorithmType) {

    if (algorithmType.equals("breadth")) {
      this.depthFirst = false;
    }
    else {
      this.depthFirst = true;
    }

    this.path = new HashMap<>();
    this.worklist = new ArrayList<>();
    this.worklist.add(maze.get(0).get(0));

    this.stepsTotal = 0;
    this.stepsToSolve = 0;

    this.aiSolvingMaze = true;
  }

  void solveMaze(int steps) {
    for (int i = 0; i < steps; i++) {

      if (this.depthFirst) {
        this.cur = this.worklist.get(this.worklist.size() - 1);
      }
      else {
        this.cur = this.worklist.get(0);
      }

      if (this.cur.findWasVisit()) {
        this.worklist.remove(this.cur);
        continue;
      }

      this.cur.setWasVisted();

      if (this.cur.checkIfOver(this.height, this.width)) {
        // reached the end of the maze
        this.aiSolvingMaze = false;
        reconstruct(this.path, this.cur);
        return;
      }
      else {

        for (int j = 0; j < 4; j++) {
          Cell n = this.cur.acessAtAdj(j);
          if (n == null || n.findWasVisit()) {
            continue;
          }

          if (cur.acessAtWalls(j)) {
            continue;
          }

          this.worklist.add(n);
          this.stepsTotal++;

          // Record the edge (cur->n) in the cameFromEdge map
          Edge edge = new Edge(this.cur, n, Math.abs(rand.nextInt()));
          n.changePath(this.path, edge);
        }
      }
    }
  }

  // generates the scene of the maze
  public WorldScene makeScene() {

    if (this.aiSolvingMaze) {
      solveMaze(this.speed);
    }

    WorldScene ws = new WorldScene(this.width * this.scale + 2, this.height * this.scale + 2);

    int lastRow = this.height - 1;
    int lastCol = this.width - 1;

    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {

        Cell cell = this.maze.get(row).get(col);

        int x = col * this.scale + this.scale / 2;
        int y = row * this.scale + this.scale / 2;
        int w = this.scale;
        int h = this.scale;
        Color c = Color.gray;

        if (row == lastRow && col == lastCol) {
          c = Color.red;
        }

        if (this.cur.checkBothCell(row, col)) {

          c = Color.orange;
        }

        else if (cell.findWasVisit()) {
          if (showVisited) {
            c = Color.cyan;
          }
          if (cell.findSolvePath()) {
            c = Color.white;
          }
        }

        ws.placeImageXY(new RectangleImage(w, h, OutlineMode.SOLID, c), x + 1, y + 1);
      }
    }

    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        Cell cell = this.maze.get(row).get(col);

        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;

        for (int i = 0; i < 4; i++) {
          boolean putWall = cell.acessAtWalls(i);
          if (!putWall) {
            continue;
          }

          // only draw the cell's top or left wall if it's an outer wall
          if (i == 0 && cell.acessAtAdj(0) != null) {
            continue;
          }
          if (i == 2 && cell.acessAtAdj(2) != null) {
            continue;
          }

          // top or bottom wall
          if (i == 0 || i == 1) {
            x = col * this.scale + this.scale / 2;
            w = this.scale;
            h = 2;

            if (i == 0) {
              y = row * this.scale;
            }
            else if (i == 1) {
              y = (row + 1) * this.scale;
            }
          }
          // left or right wall
          if (i == 2 || i == 3) {
            y = row * this.scale + this.scale / 2;
            w = 2;
            h = this.scale;

            if (i == 2) {
              x = col * this.scale;
            }
            else if (i == 3) {
              x = (col + 1) * this.scale;
            }
          }

          ws.placeImageXY(new RectangleImage(w, h, OutlineMode.SOLID, Color.black), x + 1, y + 1);
        }

      }

    }

    WorldImage timeDisplay = new TextImage("Timer: " + Math.round(this.time / 25) + " sec",
        this.scale,
        Color.BLACK);
    ws.placeImageXY(timeDisplay, this.width * this.scale / 2, this.height * this.scale + 50);
    WorldImage wrongStepsCountDisplay = new TextImage("Wrong Steps: " + this.stepsWrong + " steps",
        this.scale, Color.BLACK);
    ws.placeImageXY(wrongStepsCountDisplay, this.width * this.scale / 2,
        this.height * this.scale + 70);

    if (this.cur.checkIfOver(this.height, this.width)) {
      this.manualSolve = false;
      WorldImage resultDisplay = new TextImage("Maze Completed!", this.scale, Color.BLACK);
      ws.placeImageXY(resultDisplay, this.width * this.scale / 2, this.height * this.scale + 90);

    }

    return ws;
  }

  // updates the world state at every frame given and updates the timer accordingly
  // it is also used to construct the animation of the formation of the maze
  public void onTick() {
    if (this.stepsTotal > 0 && (this.manualSolve || this.aiSolvingMaze)) {
      this.time += 0.15;
    }
    if (!this.isFinishedBuildingMaze) {
      removeMazeWall();
    }
  }

  // user should be able to :
  // choose between breadth-first search or depth-first search
  // reset the game (possibility: reset the game without resetting the program?)
  // play manually (keys: up, down, left, right)
  public void onKeyEvent(String ke) {
    // System.out.println("key pressed: " + ke);

    if (!this.isFinishedBuildingMaze) {
      return;
    }

    // for breadh-first
    if (ke.equals("b") && !this.aiSolvingMaze && !this.manualSolve) {
      setupMazeSolver("breadth");
    }
    // for depth-first
    else if (ke.equals("d") && !this.aiSolvingMaze && !this.manualSolve) {
      setupMazeSolver("depth");
    }
    // reset
    else if (ke.equals("r")) {
      makeMaze();
    }
    // to pause the solving of the maze
    else if (ke.equals("k")) {
      this.aiSolvingMaze = !this.aiSolvingMaze;
    }

    for (int i = 0; i < 10; i++) {
      if (ke.equals(i + "")) {
        this.speed = i * i * 10;
      }
    }

    if (ke.equals("up")) {
      this.manualSolve = true;
      if (this.cur.acessAtAdj(0) != null && !this.cur.acessAtWalls(0)) {
        this.cur.setWasVisted();
        this.cur = this.cur.acessAtAdj(0);
        this.stepsTotal++;

      }
    }

    else if (ke.equals("down")) {
      this.manualSolve = true;
      if (this.cur.acessAtAdj(1) != null && !this.cur.acessAtWalls(1)) {
        this.cur.setWasVisted();
        this.cur = cur.acessAtAdj(1);
        this.stepsTotal++;
      }

    }
    else if (ke.equals("left")) {
      this.manualSolve = true;
      if (this.cur.acessAtAdj(2) != null && !this.cur.acessAtWalls(2)) {
        this.cur.setWasVisted();
        this.cur = cur.acessAtAdj(2);
        this.stepsTotal++;
      }

    }
    else if (ke.equals("right")) {
      this.manualSolve = true;
      if (this.cur.acessAtAdj(3) != null && !this.cur.acessAtWalls(3)) {
        this.cur.setWasVisted();
        this.cur = this.cur.acessAtAdj(3);
        this.stepsTotal++;
      }
    }
    if (ke.equals("v")) {
      this.showVisited = !this.showVisited;
    }
  }

  // checks if the game is over before every scene.
  public WorldEnd worldEnds() {
    return new WorldEnd(false, new WorldScene(0, 0));
  }
}

//represents the examples of mazes, edges, cells, and utils and the tests to go along with it
class ExampleMazess {

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;

  Edge edge1;
  Edge edge2;
  Edge edge3;
  Edge edge4;

  Utils u;
  MazeGameWorld mazeSmall;
  MazeGameWorld mazeSmallRec;
  MazeGameWorld mazeRec;
  MazeGameWorld mazeRec2;
  MazeGameWorld mazeSqr;

  ArrayList<Integer> links1;
  ArrayList<Integer> links2;
  ArrayList<Integer> links11;

  void initData() {

    this.cell1 = new Cell(0, 0, 1);
    this.cell2 = new Cell(1, 0, 2);
    this.cell3 = new Cell(0, 1, 3);
    this.cell4 = new Cell(1, 1, 4);
    this.cell5 = new Cell(1, 2, 5);

    this.edge1 = new Edge(this.cell1, this.cell2, 5);
    this.edge2 = new Edge(this.cell2, this.cell3, 4);
    this.edge3 = new Edge(this.cell3, this.cell4, 6);
    this.edge4 = new Edge(this.cell1, this.cell1, 2);

    this.u = new Utils();

    this.mazeSmall = new MazeGameWorld(2, 2, 2, true);
    this.mazeRec = new MazeGameWorld(5, 8, 1, true);
    this.mazeRec2 = new MazeGameWorld(8, 5, 1, true);
    this.mazeSqr = new MazeGameWorld(5, 5, 2, true);
    this.mazeSmallRec = new MazeGameWorld(2, 3, 1, true);

    this.links1 = new ArrayList<Integer>(Arrays.asList(1, 2, 2, 3, 6));
    this.links2 = new ArrayList<Integer>(Arrays.asList(0, 6, 2, 7));
    this.links11 = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4));
  }

  void testGame(Tester t) {
    new MazeGameWorld();
  }

  void testConstructorExceptionNegative(Tester t) {
    initData();
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", -10, 10, 5, true);
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", 10, -10, 5, true);
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", 10, 10, -5, true);
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", 10, 0, -5, true);
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", 0, 10, -5, true);
    t.checkConstructorException(
        new IllegalArgumentException("You cannot have a negative or zero sized maze :("),
        "MazeGameWorld", 10, 10, 0, true);

  }

  void testConstructorExceptionScale(Tester t) {
    initData();
    t.checkConstructorException(
        new IllegalArgumentException("Scale cannot be greater than your width"), "MazeGameWorld",
        10, 10, 15, true);
    t.checkConstructorException(
        new IllegalArgumentException("Scale cannot be greater than your width"), "MazeGameWorld", 5,
        10, 40, true);
  }

  void testConstructorExceptionEdge(Tester t) {
    initData();
    initData();
    t.checkConstructorException(new IllegalArgumentException("weight cannot be negative"), "Edge",
        this.cell1, this.cell2, -10);
    t.checkConstructorException(new IllegalArgumentException("weight cannot be negative"), "Edge",
        this.cell2, this.cell3, -1);
  }

  void testConstructorExceptionCell(Tester t) {
    initData();
    t.checkConstructorException(
        new IllegalArgumentException("your row col or id cannot be negative"), "Cell", 0, -1, 2);
    t.checkConstructorException(
        new IllegalArgumentException("your row col or id cannot be negative"), "Cell", -1, 1, 2);
    t.checkConstructorException(
        new IllegalArgumentException("your row col or id cannot be negative"), "Cell", 3, 1, -2);
  }

  void testSetAdj(Tester t) {
    initData();
    this.cell1.setAdj(0, this.cell1);
    t.checkExpect(this.cell1.adj.get(0), this.cell1);
    this.cell1.setAdj(1, this.cell4);
    t.checkExpect(this.cell1.adj.get(1), this.cell4);
    t.checkExpect(this.cell1.adj.get(3), null);
    this.cell3.setAdj(3, this.cell2);
    t.checkExpect(this.cell3.adj.get(3), this.cell2);
  }

  void testSetWalls(Tester t) {
    initData();
    this.cell1.setWalls(0, false);
    t.checkExpect(this.cell1.walls.get(0), false);
    this.cell1.setWalls(1, true);
    t.checkExpect(this.cell1.walls.get(1), true);
    t.checkExpect(this.cell1.walls.get(3), true);
    this.cell3.setWalls(3, false);
    t.checkExpect(this.cell3.walls.get(3), false);
  }

  void testCheckCellEqual(Tester t) {
    initData();
    this.cell1.checkCellEqual(this.cell2, this.mazeSmall.maze);
    t.checkExpect(this.mazeSmall.maze.get(0).get(0).walls.get(1), false);
    t.checkExpect(this.mazeSmall.maze.get(0).get(1).walls.get(0), true);
    initData();
    this.cell1.checkCellEqual(cell3, this.mazeSmall.maze);
    t.checkExpect(this.mazeSqr.maze.get(0).get(0).walls.get(3), true);
    t.checkExpect(this.mazeSqr.maze.get(0).get(1).walls.get(2), true);
  }

  void testFindRoot(Tester t) {
    initData();
    t.checkExpect(this.cell1.findRoot(links1, 2), 2);
    t.checkExpect(this.cell2.findRoot(links2, 0), 0);
    t.checkExpect(this.cell4.findRoot(links1, 3), 3);
    t.checkExpect(this.cell3.findRoot(links1, 2), 2);
  }

  void testFindRootHelp(Tester t) {
    initData();
    t.checkExpect(this.cell1.findRootHelp(links11), 1);
    t.checkExpect(this.cell2.findRootHelp(links11), 2);
    t.checkExpect(this.cell2.findRootHelp(links2), 2);
    t.checkExpect(this.cell3.findRootHelp(links11), 3);
  }

  void testFindRootTo(Tester t) {
    initData();
    t.checkExpect(this.edge1.findRootTo(links1), 2);
    t.checkExpect(this.edge3.findRootTo(links11), 4);
    t.checkExpect(this.edge2.findRootTo(links11), 3);
    t.checkExpect(this.edge4.findRootTo(links1), 2);
  }

  void testFindRootFrom(Tester t) {
    initData();
    t.checkExpect(this.edge1.findRootFrom(links1), 2);
    t.checkExpect(this.edge3.findRootFrom(links11), 3);
    t.checkExpect(this.edge2.findRootFrom(links11), 2);
    t.checkExpect(this.edge4.findRootFrom(links1), 2);
  }

  void testCompare(Tester t) {
    initData();
    t.checkExpect(this.u.compare(edge2, edge1), -1);
    t.checkExpect(this.u.compare(edge1, edge1), 0);
    t.checkExpect(this.u.compare(edge1, edge4), 3);
  }

  void testMakeMaze(Tester t) {
    initData();
    ArrayList<ArrayList<Cell>> sampMaze = this.mazeSmall.maze;

    t.checkExpect(sampMaze.size(), this.mazeSmall.maze.size());
    t.checkExpect(sampMaze.get(0).size(), this.mazeSmall.maze.size());
    t.checkExpect(sampMaze.get(0).size(), this.mazeSmall.maze.size());
    t.checkExpect(sampMaze.get(0).get(0).walls.get(0), true);
    t.checkExpect(sampMaze.get(1).get(0).walls.get(0), true);
    t.checkExpect(sampMaze.get(0).get(0).adj.contains(sampMaze.get(1).get(0)), true);
    t.checkExpect(sampMaze.get(0).get(0).adj.contains(sampMaze.get(0).get(1)), true);
    t.checkExpect(sampMaze.get(1).get(0).adj.contains(sampMaze.get(0).get(0)), true);
    t.checkExpect(sampMaze.get(1).get(0).adj.contains(sampMaze.get(1).get(1)), true);
    t.checkExpect(sampMaze.get(0).get(0).adj.contains(sampMaze.get(1).get(1)), false);
    initData();
    ArrayList<ArrayList<Cell>> sampMaze2 = this.mazeRec.maze;
    t.checkExpect(sampMaze2.size(), this.mazeRec.maze.size());
    t.checkExpect(sampMaze2.get(0).size(), this.mazeRec.width);
    t.checkExpect(sampMaze2.get(1).size(), this.mazeRec.width);
    t.checkExpect(sampMaze2.get(0).get(0).walls.get(0), true);
    t.checkExpect(sampMaze2.get(1).get(0).walls.get(1), true);
    t.checkExpect(sampMaze2.get(0).get(1).walls.get(0), true);
    t.checkExpect(sampMaze2.get(1).get(1).walls.get(1), true);
    t.checkExpect(sampMaze2.get(2).get(0).walls.get(0), true);
    t.checkExpect(sampMaze2.get(2).get(1).walls.get(1), true);
    t.checkExpect(sampMaze2.get(2).get(2).walls.get(0), true);
    t.checkExpect(sampMaze2.get(0).get(2).walls.get(1), true);
    t.checkExpect(sampMaze2.get(1).get(1).walls.get(0), true);
    t.checkExpect(sampMaze2.get(2).get(0).walls.get(1), true);

    t.checkExpect(sampMaze.get(0).get(0).adj.contains(sampMaze.get(1).get(0)), true);
    t.checkExpect(sampMaze.get(0).get(0).adj.contains(sampMaze.get(0).get(1)), true);
    t.checkExpect(sampMaze.get(1).get(0).adj.contains(sampMaze.get(0).get(0)), true);
    t.checkExpect(sampMaze.get(1).get(0).adj.contains(sampMaze.get(1).get(1)), true);
  }

  void testMakeScene(Tester t) {
    initData();
    WorldScene ws1 = new WorldScene(this.mazeSmall.width * this.mazeSmall.scale + 2,
        this.mazeSmall.height * this.mazeSmall.scale + 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.orange), 2, 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.gray), 4, 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.gray), 2, 4);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.red), 4, 4);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 2, 1);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 2, 3);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 1, 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 3, 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 4, 1);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 4, 3);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 5, 2);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 2, 5);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 1, 4);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 3, 4);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 4, 5);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 5, 4);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 2, 54);
    ws1.placeImageXY(new RectangleImage(2, 2, OutlineMode.SOLID, Color.black), 2, 74);
    t.checkExpect(this.mazeSmall.makeScene(), ws1);
    initData();
    WorldScene ws2 = new WorldScene(this.mazeSmallRec.width * this.mazeSmallRec.scale + 2,
        this.mazeSmallRec.height * this.mazeSmallRec.scale + 2);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 1, 1);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 1, 1);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 2, 1);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 2, 2);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 3, 1);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 1, 3);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 1, 2);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 3, 2);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 1, 4);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 1, 3);
    ws2.placeImageXY(new RectangleImage(1, 2, OutlineMode.SOLID, Color.black), 2, 4);
    ws2.placeImageXY(new RectangleImage(2, 1, OutlineMode.SOLID, Color.black), 3, 3);
    t.checkExpect(this.mazeSmallRec.makeScene(), ws2);

  }

  void testFindFrom(Tester t) {
    initData();
    t.checkExpect(this.edge1.findFrom(), this.cell1);
    t.checkExpect(this.edge2.findFrom(), this.cell2);
    t.checkExpect(this.edge3.findFrom(), this.cell3);
  }

  void testCellPathTrue(Tester t) {
    initData();
    this.cell1.cellPathTrue();
    t.checkExpect(cell1.isInSolvePath, true);
    this.cell2.cellPathTrue();
    t.checkExpect(cell2.isInSolvePath, true);
    t.checkExpect(cell3.isInSolvePath, false);
    this.cell3.cellPathTrue();
    t.checkExpect(cell3.isInSolvePath, true);
  }

  void testWasVisited(Tester t) {
    initData();
    this.cell1.setWasVisted();
    t.checkExpect(cell1.wasVisited, true);
    this.cell2.setWasVisted();
    t.checkExpect(cell2.wasVisited, true);
    t.checkExpect(cell3.wasVisited, false);
    this.cell3.setWasVisted();
    t.checkExpect(cell3.wasVisited, true);
  }

  void testCheckBothCell(Tester t) {
    initData();
    t.checkExpect(this.cell3.checkBothCell(2, 1), false);
    t.checkExpect(this.cell4.checkBothCell(1, 1), true);
  }

  void testMakeEdge(Tester t) {
    initData();
    HashMap<Integer, Edge> path = new HashMap<Integer, Edge>();
    Edge edge1 = new Edge(this.cell1, this.cell2, 1);
    Edge edge2 = new Edge(this.cell2, this.cell3, 0);
    path.put(1, edge1);
    path.put(2, edge2);
    t.checkExpect(cell1.makeEdge(path), edge1);
    t.checkExpect(cell2.makeEdge(path), edge2);
  }

  void testWasVisit(Tester t) {
    initData();
    t.checkExpect(this.cell1.findWasVisit(), false);
    this.cell1.setWasVisted();
    t.checkExpect(this.cell1.findWasVisit(), true);
    t.checkExpect(this.cell2.findWasVisit(), false);
    this.cell2.setWasVisted();
    t.checkExpect(this.cell2.findWasVisit(), true);
  }

  void testCheckIfOver(Tester t) {
    initData();
    t.checkExpect(this.cell2.checkIfOver(1, 1), false);
    t.checkExpect(this.cell2.checkIfOver(2, 1), true);
  }

  void testChangePath(Tester t) {
    initData();
    HashMap<Integer, Edge> path = new HashMap<Integer, Edge>();
    Edge edge1 = new Edge(this.cell1, this.cell2, 1);
    Edge edge2 = new Edge(this.cell2, this.cell3, 0);
    path.put(1, edge1);
    path.put(2, edge2);
    t.checkExpect(path.get(1), edge1);
    t.checkExpect(path.get(2), edge2);
  }

  void testFindSolvePath(Tester t) {
    initData();
    t.checkExpect(this.cell1.findSolvePath(), false);
    this.cell1.isInSolvePath = true;
    t.checkExpect(this.cell1.findSolvePath(), true);
  }

  /*
   */

  void testRemoveMazeWall(Tester t) {
    initData();
    this.mazeRec.posInTree = 1;
    this.mazeRec.tree = new ArrayList<Edge>(Arrays.asList(this.edge1, this.edge2));
    this.mazeRec.removeMazeWall();
    t.checkExpect(this.mazeRec.posInTree, 2);
    this.mazeSqr.posInTree = 3;
    this.mazeSqr.tree = new ArrayList<Edge>(Arrays.asList(this.edge3, this.edge4));
    this.mazeSqr.removeMazeWall();
    t.checkExpect(this.mazeSqr.posInTree, 3);
  }

  void testReconstructMaze(Tester t) {
    initData();
    HashMap<Integer, Edge> path = new HashMap<Integer, Edge>();
    this.mazeRec.reconstruct(path, this.cell1);
    this.mazeRec.stepsTotal = 10;
    this.mazeRec.stepsToSolve = 3;
    t.checkExpect(this.mazeRec.stepsWrong, -1);

    this.mazeSqr.stepsTotal = 15;
    this.mazeSqr.stepsToSolve = 7;
    this.mazeSqr.reconstruct(path, this.cell1);

    t.checkExpect(this.mazeSqr.stepsWrong, 7);
  }

  void testMazeSolver(Tester t) {
    initData();
    this.mazeRec.worklist = new ArrayList<Cell>();
    this.mazeRec.worklist.add(this.cell1);
    this.mazeRec.worklist.add(this.cell2);
    this.mazeRec.worklist.add(this.cell3);
    this.mazeRec.worklist.add(this.cell4);

    this.mazeRec.solveMaze(2);

    t.checkExpect(this.mazeRec.cur, this.cell4);
    t.checkExpect(this.mazeRec.cur.wasVisited, true);
    t.checkExpect(this.mazeRec.aiSolvingMaze, false);
    t.checkExpect(this.mazeSqr.stepsTotal, 0);

    initData();
    this.mazeSqr.worklist = new ArrayList<Cell>();
    this.mazeSqr.worklist.add(this.cell1);
    this.mazeSqr.worklist.add(this.cell4);
    this.mazeSqr.worklist.add(this.cell2);

    this.mazeSqr.solveMaze(1);

    t.checkExpect(this.mazeSqr.cur, this.cell2);
    t.checkExpect(this.mazeSqr.cur.wasVisited, true);
    t.checkExpect(this.mazeSqr.aiSolvingMaze, false);
    t.checkExpect(this.mazeSqr.stepsTotal, 0);
  }

  void testSetUpMazeSolver(Tester t) {
    initData();

    this.mazeRec.setupMazeSolver("breadth");
    t.checkExpect(this.mazeRec.depthFirst, false);
    t.checkExpect(this.mazeRec.worklist.get(0), this.mazeRec.maze.get(0).get(0));
    t.checkExpect(this.mazeRec.aiSolvingMaze, true);

    this.mazeSqr.setupMazeSolver("depth");
    t.checkExpect(this.mazeSqr.depthFirst, true);
    t.checkExpect(this.mazeSqr.worklist.get(0), this.mazeSqr.maze.get(0).get(0));
    t.checkExpect(this.mazeSqr.aiSolvingMaze, true);
  }

  void testOnKeyEvent(Tester t) {
    initData();
    this.mazeRec.onKeyEvent("d");
    t.checkExpect(this.mazeRec.depthFirst, true);
    this.mazeRec.depthFirst = false;
    this.mazeRec.onKeyEvent("b");
    t.checkExpect(this.mazeRec.depthFirst, false);
    ArrayList<ArrayList<Cell>> before = this.mazeRec.maze;
    this.mazeRec2.maze = before;
    this.mazeRec.onKeyEvent("r");
    this.mazeRec2.makeMaze();
    t.checkExpect(before, this.mazeRec.maze);
    this.mazeRec.onKeyEvent("k");
    t.checkExpect(this.mazeRec.aiSolvingMaze, false);
    initData();
    this.mazeSqr.isFinishedBuildingMaze = true;

    this.mazeSqr.onKeyEvent("up");
    t.checkExpect(this.mazeSqr.manualSolve, true);
    t.checkExpect(this.mazeSqr.cur.wasVisited, false);
    this.mazeRec.isFinishedBuildingMaze = true;
    this.mazeRec.onKeyEvent("down");
    t.checkExpect(this.mazeRec.manualSolve, true);
    t.checkExpect(this.mazeRec.cur.wasVisited, false);

    this.mazeSqr.onKeyEvent("left");
    t.checkExpect(this.mazeSqr.manualSolve, true);
    t.checkExpect(this.mazeSqr.cur.wasVisited, false);
    this.mazeRec.isFinishedBuildingMaze = true;
    this.mazeRec.onKeyEvent("right");
    t.checkExpect(this.mazeRec.manualSolve, true);
    t.checkExpect(this.mazeRec.cur.wasVisited, false);

    this.mazeSqr.onKeyEvent("v");
    t.checkExpect(this.mazeSqr.showVisited, false);
  }

  void testWorldEnd(Tester t) {
    initData();
    t.checkExpect(this.mazeSmall.worldEnds(), new WorldEnd(false, new WorldScene(0, 0)));
    t.checkExpect(this.mazeRec.worldEnds(), new WorldEnd(false, new WorldScene(0, 0)));
    t.checkExpect(this.mazeSqr.worldEnds(), new WorldEnd(false, new WorldScene(0, 0)));

  }
}