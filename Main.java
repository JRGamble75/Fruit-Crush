import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/* 
 * swap algorithm
 * get both nodes
 * get rect shapes
 * set nodes invisible
 * paint shapes on top of now invisible nodes
 * animate transition
 * set shapes invisible
 * update grid
 */

public class Main extends Application {
	/** 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	//Global Variables
	private final int GRID_SIZE = 8;
	private Tile[][] allTiles;
	private int score = 0;
	public int swapCount = 0;
	public int bombSupply = 2;

	Random rand = new Random();
	boolean isFirstTile = true;	//Boolean for first or second click
	Node n1, n2;
	Integer r1, c1, r2, c2;
	Rectangle rect1, rect2;
	Rectangle[][] rectGrid = new Rectangle[GRID_SIZE][GRID_SIZE];	//Set size of rectangle
	GridPane grid = new GridPane();
	Scene scene;

	//Images for tiles and background
	Image apple = new Image("file:CSC 330 GP\\Tile images\\AppleTile.jpg");
	Image banana = new Image("file:CSC 330 GP\\Tile images\\BananaTile.jpg");
	Image blueberry = new Image("file:CSC 330 GP\\Tile images\\BlueberryTile.jpg");
	Image grape = new Image("file:CSC 330 GP\\Tile images\\GrapeTile.jpg");
	Image orange = new Image("file:CSC 330 GP\\Tile images\\MangoTile.jpg");
	Image peach = new Image("file:CSC 330 GP\\Tile images\\PeachTile.jpg");
	Image pear = new Image("file:CSC 330 GP\\Tile images\\PearTile.jpg");
	Image watermelonSlice = new Image("file:CSC 330 GP\\Tile images\\WatermelonSliceTile.jpg");
	Image nullTile = new Image("file:CSC 330 GP\\Tile images\\NullTile.jpg");
	Image BackGround = new Image("file:CSC 330 GP\\Accessory Images\\PokemonBg.jpg", 650, 450, false, true);
	ImageView melonSliceIV = new ImageView(watermelonSlice);
	
	Label PlayerScore;
	Label BombSupply;
	HBox hbox;
	VBox rootBox;
	PowerUp bomb = new Bomb();
	FileChooser fc = new FileChooser();
	File file;
	
	/** 
	 * @param primaryStage
	 */
	public void start(Stage primaryStage) {	
		
		initModel(); 
		printModel();		
		//Go through the model, using its values to initialize the view
		for(int row = 0; row < GRID_SIZE; row++) {
			for(int col = 0; col < GRID_SIZE; col++) {
				Rectangle rect = new Rectangle(50,50);
				rect.setFill(new ImagePattern(getImage(row, col)));  //Fill all tiles with their images
				rect.setStrokeWidth(5);	//Set stroke width to 5
				rect.setStroke(Color.TRANSPARENT);	//Set stroke for all tiles to transparent
				//Create event handler for rect
				rect.setOnMouseClicked(e -> {
					updateGrid();
					if(isFirstTile) {
						removeHighlight();	//remove highlight from previous click
						rect.setStroke(Color.YELLOW);	//add highlight to click
						n1 = (Node)e.getSource();     //get the Node living in the gridpane
						r1 = GridPane.getRowIndex(n1);     //get the node's row
						c1 = GridPane.getColumnIndex(n1);  //get the node's column
						isFirstTile = false;	//set for second click
						System.out.println("First Tile Selected " + r1 + " " + c1);
					} else {
						removeHighlight(); 	//remove highlight from previous click
						rect.setStroke(Color.YELLOW);	//add highlight to click
						n2 = (Node)e.getSource();     //get the Node living in the gridpane
						r2 = GridPane.getRowIndex(n2);     //get the node's row
						c2 = GridPane.getColumnIndex(n2);	//get the node's column
						isFirstTile = true;		//set for first click
						System.out.println("Second Tile Selected " + r2 + " " + c2);
						
						//Checks whether the second tile clicked is compatible for swap
						if (Math.abs(r1 - r2) == 1 && c2 == c1 || Math.abs(c1 - c2) == 1 && r2 == r1) {
							swap(r1, c1, r2, c2);
							
							//Will swap back if the first swap did not result in a match
							if(checkForMatch() == false) {
								swap(r1, c1, r2, c2);
								removeHighlight();
							}
							removeHighlight();
						}
						else System.out.println("Invalid tile selected");

						//Check for matches 
						//If matches are found tiles will be set to null
						//Null tiles will rise to top
						//If a Null tile is at the top it will be re-randomized to new tile
						while (checkForMatch()) {
							nullify();
							gravity();
							populateTiles();
						}				
						//Print the change in the model.
						System.out.println("After the click, the model is: \n");
						printModel();
					}
				});
				rectGrid[row][col] = rect;
				grid.add(rect, col, row);
			}
		}
		
		//Create a label for the players score
		Label ScoreTitle = new Label("SCORE");
        PlayerScore = new Label("0");
        ScoreTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: black");
        PlayerScore.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: black");
        
		//Create a label for the supply of Fruit Bombs
        BombSupply = new Label(String.format("%d", bombSupply));
		BombSupply.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: black");

		//Create new button for Fruit Bombs
		Button Fbomb = new Button("Fruit Bomb");

		//Create event handler for Fruit Bomb
		Fbomb.setOnAction(e -> {
			if(bombSupply > 0){
				bomb.Use(allTiles);
				do {
					nullify();
					gravity();
					populateTiles();
				} while (checkForMatch());
				printModel();
				bombSupply--;
			}	
		});

		//Create new VBox for the Score
		VBox ScoreBox = new VBox(ScoreTitle, PlayerScore); 
		ScoreBox.setAlignment(Pos.CENTER);
		
		//Create new VBox for left side of screen
		VBox vbox = new VBox(melonSliceIV, ScoreBox, Fbomb, BombSupply);
		vbox.setAlignment(Pos.TOP_CENTER);
		
		//Create Menu Bar
		MenuBar menuBar = new MenuBar();
		 
		//Create the File menu.
		Menu fileMenu = new Menu("File");
	    MenuItem exitItem = new MenuItem("Exit");
	    MenuItem saveItem = new MenuItem("Save");
	    MenuItem loadItem = new MenuItem("Load");
	    
		//Add save, load, and exit to menu
	    fileMenu.getItems().add(saveItem);
	    fileMenu.getItems().add(loadItem);
	    fileMenu.getItems().add(exitItem);

		//Register an event handler for the save action
	    saveItem.setOnAction(event -> {
	    	try {		
				file = fc.showSaveDialog(primaryStage);
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
				output.writeInt(score);		//Save score to file
				output.writeInt(bombSupply);	//Save supply of Fruit bombs
				output.writeObject(rectGrid);	//Save grid to file
				output.writeObject(allTiles);	//Save tiles to file
				output.close();		//Close file output stream
			} catch (Exception e) {
				System.out.println("Error writing board to file.");
				e.printStackTrace();
			}	
	    	System.out.println("Saved Board");
	    	printModel();
	    });

		//Register an event handler for the load action
	    loadItem.setOnAction(event -> {
	    	System.out.println("Before Load");
	    	printModel();
	    	try {				
				file = fc.showOpenDialog(primaryStage);
				ObjectInputStream oit = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
				score = oit.readInt();	//Load score from file
				bombSupply = oit.readInt();	//Load supply of Fruit bombs
				rectGrid = (Rectangle[][])oit.readObject();	//Load grid from file
				allTiles = (Tile[][])oit.readObject();	//Load tiles from file
				oit.close();	//Close file input stream
			} catch (Exception e) {
				System.out.println("Error!!!!");
				e.printStackTrace();
			}
	    	System.out.println("After Load");
			printModel();
	    	updateGrid();
	    });
      
	    //Register an event handler for the exit item.
	    exitItem.setOnAction(event -> {
	        primaryStage.close();
	    });

	    //Add the File menu to the menu bar.
	    menuBar.getMenus().addAll(fileMenu);
	         
	    //Add the menu bar to a BorderPane.
	    BorderPane borderPane = new BorderPane();
	    borderPane.setTop(menuBar);
		
		//Make new hbox 
	    hbox = new HBox(10, vbox, grid);	//hbox has vbox on left and grid on right

		//Create background image
		BackgroundImage myB = new BackgroundImage(BackGround, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		hbox.setBackground(new Background(myB));	//Set background image
		
		rootBox = new VBox(borderPane, hbox);	//rootBox will have borderPane above hbox
		scene = new Scene(rootBox);		//Scene will have dimensions equal to the rootBox

		primaryStage.getIcons().add(watermelonSlice);	//Adds watermelon icon to tab
		primaryStage.setScene(scene);	//Set scene
		primaryStage.setTitle("Fruit Crush");	//Names the tab Fruit Crush
        primaryStage.show();	//Shows the scren to user
	}
	
	/*
	 * This function will randomize the grid filling with tiles, 
	 * and will not finish until the grid has no matches shown
	 */
	private void initModel() {
		allTiles = new Tile[GRID_SIZE][GRID_SIZE];
		//initialize model
		do {
			for(int row = 0; row < GRID_SIZE; row++) {
				for(int col = 0; col < GRID_SIZE; col++) {
					int randNum = rand.nextInt(7);
					if (randNum == 0) {
						allTiles[row][col] = new RedTile();
					} else if (randNum == 1) {
						allTiles[row][col] = new BlueTile();
					} else if (randNum == 2) {
						allTiles[row][col] = new GreenTile();
					} else if (randNum == 3) {
						allTiles[row][col] = new YellowTile();
					} else if (randNum == 4) {
						allTiles[row][col] = new PinkTile();
					} else if (randNum == 5) {
						allTiles[row][col] = new PurpleTile();
					} else if (randNum == 6) {
						allTiles[row][col] = new OrangeTile();
					}
				}
			}
		}while(checkForMatch());
		bombSupply = 2;
        score = 0;
	}
	
	/*
	 * Will remove the highlight around the first node clicked
	 */
	private void removeHighlight() {
		for(Node node : grid.getChildren()) {
			Integer r = GridPane.getRowIndex(node);
			Integer c = GridPane.getColumnIndex(node);
			rectGrid[r][c].setStroke(Color.TRANSPARENT);
			rectGrid[r][c] = (Rectangle)node;
	    }
	}

	/*
	 * This will update score and supply of Fruit Bombs after every move
	 * This will also add the image of whatever new tiles there are
	 */
	public void updateGrid() {
		PlayerScore.setText(String.format("%d", score));
		BombSupply.setText(String.format("%d", bombSupply));
		for(Node node : grid.getChildren()) {
			int r = GridPane.getRowIndex(node);
			int c = GridPane.getColumnIndex(node);
			rectGrid[r][c].setFill(new ImagePattern(getImage(r, c)));
			rectGrid[r][c] = (Rectangle)node;
			node.setVisible(true);
	    }
	}

	/** 
	 * @param r
	 * @param c
	 * @return Image
	 * Will fill each tile with its corresponding image
	 */
	public Image getImage(int r, int c) {
		if(allTiles[r][c].getType().equals("Red"))
			return apple;
		if(allTiles[r][c].getType().equals("Green"))
			return pear;
		if(allTiles[r][c].getType().equals("Blue"))
			return blueberry;
		if(allTiles[r][c].getType().equals("Pink"))
			return peach;
		if(allTiles[r][c].getType().equals("Yellow"))
			return banana;
		if(allTiles[r][c].getType().equals("Purple"))
			return grape;
		if(allTiles[r][c].getType().equals("Orange"))
			return orange;
		return nullTile;
	}
	
	/*
	 * Will print the grid using the first letter in the tiles Type
	 */
	public void printModel() {
		for(int row = 0; row < GRID_SIZE; row++) {
			for(int col = 0; col < GRID_SIZE; col++) {
				System.out.print(allTiles[row][col] + " ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("SwapCount:" + swapCount);
	}
	
	/** 
	 * @return boolean
	 * Loop through rows and columns checking every tile and the tile 2 away
	 * if those two are a match then check the tile inbetween
	 * if that is a match then the 4 and 5 corresponding tiles are also checked for a match
	 */
	public boolean checkForMatch() {
		boolean match = false;
		for(int r = 0; r < GRID_SIZE; r++) {
			for(int c = 0; c < GRID_SIZE; c++) {	
				if (c + 2 <= GRID_SIZE - 1 && allTiles[r][c].compareTo(allTiles[r][c + 2]) == 0 && !(allTiles[r][c].getType().equals ("Null"))) {
					if (c + 2 <= GRID_SIZE - 1 && allTiles[r][c + 1].compareTo(allTiles[r][c + 2]) == 0) {
						if (c + 3 <= GRID_SIZE - 1 && allTiles[r][c + 2].compareTo(allTiles[r][c + 3]) == 0) {
							if (c + 4 <= GRID_SIZE - 1 && allTiles[r][c + 3].compareTo(allTiles[r][c + 4]) == 0) {
								score = score + 250;
								allTiles[r][c + 4].isMatch = true;
							}
							score = score + 150;
							allTiles[r][c + 3].isMatch = true;
							bombSupply = bombSupply + 1;
						} 
						allTiles[r][c + 2].isMatch = true;
						allTiles[r][c + 1].isMatch = true;
						allTiles[r][c].isMatch = true;
						score = score + 100;
						match = true;
					}
				}
			}			
		}
		for(int c = 0; c < GRID_SIZE; c++) {
			for(int r = 0; r < GRID_SIZE; r++) {
				if (r + 2 <= GRID_SIZE - 1 && allTiles[r][c].compareTo(allTiles[r + 2][c]) == 0 && !(allTiles[r][c].getType().equals ("Null"))) {
					if (r + 2 <= GRID_SIZE - 1 && allTiles[r + 1][c].compareTo(allTiles[r + 2][c]) == 0) {
						if (r + 3 <= GRID_SIZE - 1 && allTiles[r + 2][c].compareTo(allTiles[r + 3][c]) == 0) {
							if (r + 4 <= GRID_SIZE - 1 && allTiles[r + 3][c].compareTo(allTiles[r + 4][c]) == 0) {
								score = score + 250;
								allTiles[r + 4][c].isMatch = true;
							}
							score = score + 150;
							allTiles[r + 3][c].isMatch = true;
							bombSupply = bombSupply + 1;
						} 
						allTiles[r + 2][c].isMatch = true;
						allTiles[r + 1][c].isMatch = true;
						allTiles[r][c].isMatch = true;
						match = true;
					}
				}
			}			
		}
		System.out.println("Score is: " + score);
		return match;
	}

	/*
	 * Will loop through the grid and if any tile is null will move it to the top
	 */
    public void gravity() {
		for (int col = 0; col < GRID_SIZE; col++)
        {
            //Loop through each tile in column from bottom to top
            for (int row = GRID_SIZE - 1; row >= 0; row--)
            {
                //If this space is blank, but the one above it is not, swap tiles
                if(row - 1 >= 0 && allTiles[row][col].getType() == "Null" && allTiles[row-1][col].getType() != "Null")
                {
					swap(row, col, row - 1, col);
                    row = GRID_SIZE;
                }
            }
        }
	}

	/*
	 * Will find all tiles that have made a match and animate them,
	 * next the tiles will become nullTiles
	 */
	public void nullify() {
		//Add popAnimation to all tiles that are a match
		for(int r = 0; r < GRID_SIZE; r++) {
			for(int c = 0; c < GRID_SIZE; c++) { 
				if(allTiles[r][c].isMatch) {
					popAnimation(r,c);
				}
			}
		}
		//Nullify all tiles in the match
		for(int r = 0; r < GRID_SIZE; r++) {
			for(int c = 0; c < GRID_SIZE; c++) { 
				if(allTiles[r][c].isMatch) {
					allTiles[r][c] = new nullTile();
				}
			}
		}
	}
	
	/** 
	 * @param row
	 * @param col
	 * This adds an animation to make the node increase in size
	 */
	private void popAnimation(int row, int col) {
		updateGrid();
		for(Node node : grid.getChildren()) {
			Integer r = GridPane.getRowIndex(node);
			Integer c = GridPane.getColumnIndex(node);
			if(r == row && c == col) {
				//Create new transition
				ScaleTransition strans = new ScaleTransition(new Duration(500), node);

				//Starting point
				strans.setFromX(1.0);
				strans.setFromY(1.0);

				//Ending point
				strans.setToX(1.5);
				strans.setToY(1.5);

				//Create event handler for transition
				strans.setOnFinished(e->{
					updateGrid();
			    });
				strans.setCycleCount(2);	//Cycles the animation twice
				strans.setAutoReverse(true);	//Set reverse to true
				strans.play();	//Play animation
			}
		}
	}
	
	/** 
	 * @return boolean 
	 * loops through top row to clear all null tiles
	 */
	private boolean noNulls() {
		Tile nullT = new nullTile();
		for (int c = 0; c < GRID_SIZE; c++) {
			if(allTiles[0][c].compareTo(nullT) == 0)
				return true;
		}
		return false;
	}

	/*
	 * while the noNulls fuction is called all null tiles will be
	 * re populated with random tiles and then dropped to the 
	 * lowest spot thet can go
	 */
	public void populateTiles() {
		Tile nullT = new nullTile();
		while(noNulls()) {
			//printModel();
			for (int c = 0; c < GRID_SIZE; c++) {
				if (allTiles[0][c].compareTo(nullT) == 0){ // reroll nulltile
					int randNum = rand.nextInt(7);
					if (randNum == 0) {
						allTiles[0][c] = new RedTile();
					} else if (randNum == 1) {
						allTiles[0][c] = new BlueTile();
					} else if (randNum == 2) {
						allTiles[0][c] = new GreenTile();
					} else if (randNum == 3) {
						allTiles[0][c] = new YellowTile();
					} else if (randNum == 4) {
						allTiles[0][c] = new PinkTile();
					} else if (randNum == 5) {
						allTiles[0][c] = new PurpleTile();
					} else if (randNum == 6) {
						allTiles[0][c] = new OrangeTile();
					}
				}
			}
			gravity();
		}
	}
	
	/** 
	 * @param r1
	 * @param c1
	 * @param r2
	 * @param c2
	 * Compares String Type if there is a match then, 
	 * the first tile will have the same type as the second and,
	 * the second tile will have the same type as the first 
	 */
	public void swap(Integer r1, Integer c1, Integer r2, Integer c2) {
		String type1 = allTiles[r1][c1].getType();
		String type2 = allTiles[r2][c2].getType();
		
		if (type1.equals ("Red"))
			allTiles[r2][c2] = new RedTile();
		if (type1.equals ("Blue"))
			allTiles[r2][c2] = new BlueTile();
		if (type1.equals ("Green"))
			allTiles[r2][c2] = new GreenTile();
		if (type1.equals ("Yellow"))
			allTiles[r2][c2] = new YellowTile();
		if (type1.equals ("Pink"))
			allTiles[r2][c2] = new PinkTile();
		if (type1.equals ("Purple"))
			allTiles[r2][c2] = new PurpleTile();
		if (type1.equals ("Orange"))
			allTiles[r2][c2] = new OrangeTile();
        if (type1.equals ( "Null"))
            allTiles[r2][c2] = new nullTile();

		if (type2.equals ("Red"))
			allTiles[r1][c1] = new RedTile();
		if (type2.equals ( "Blue"))
			allTiles[r1][c1] = new BlueTile();
		if (type2.equals ("Green"))
			allTiles[r1][c1] = new GreenTile();
		if (type2.equals ("Yellow"))
			allTiles[r1][c1] = new YellowTile();
		if (type2.equals ("Pink"))
			allTiles[r1][c1] = new PinkTile();
		if (type2.equals ("Purple"))
			allTiles[r1][c1] = new PurpleTile();
		if (type2.equals ("Orange"))
			allTiles[r1][c1] = new OrangeTile();
        if (type2.equals ("Null"))
            allTiles[r1][c1] = new nullTile();
        
        swapCount = swapCount + 1;
	}
}