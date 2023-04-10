package fifteenpuzzle;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;


// Board state class
class BoardState implements Comparable<BoardState>{
	int[][] board;
	int blankRow, blankCol;
	int cost, estimatedCost;

	String moveBefore;
	BoardState parent;
	public BoardState(int[][] board, int blankRow, int blankCol, int cost, int estimatedCost, String move, BoardState parent) {
		this.board = board;
		this.blankRow = blankRow;
		this.blankCol = blankCol;
		this.cost = cost;
		this.estimatedCost = estimatedCost;
		this.moveBefore = move;
		this.parent = parent;
	}

	// implement compareTo for priority queue (compare by cost)
	public int compareTo(BoardState other) {
		return (this.cost + this.estimatedCost) - (other.cost + other.estimatedCost);
	}

	// board to string
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int[] row : board) {
			for (int val : row) {
				sb.append(val).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
public class Solver {

	static int[][] board;
	static int blankRow;
	static int blankCol;
	static int SIZE;

	public static void ReadBoard(File input){

		try {
			BufferedReader br = new BufferedReader(new FileReader(input));

			// get size of board (first line)
			int size = Integer.parseInt(br.readLine());
			SIZE = size;
			// set size of board
			board = new int[size][size];

			// set all values to the board

			// 15 puzzle board has c1,c2 then a space repeat size times for row and another
			// size times for columns
			int c1, c2, s;
			for(int i = 0; i < size; i++){
				for(int j = 0; j < size; j++){
					c1 = br.read();
					c2 = br.read();
					s = br.read(); // space placeholder

					// check if characters are = 0
					if (c1 == ' ')
						c1 = '0';
					if (c2 == ' ') {
						// this is where the empty space is
						// keep track of row and col
						c2 = '0';
						blankRow = i;
						blankCol = j;
					}

					// add to board
					board[i][j] = 10 * (c1 - '0') + (c2 - '0');

				}
			}


		} catch(FileNotFoundException e){
			System.out.println("File not found/ incorrect name passed" + input.getName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static ArrayList<String> solveBoard(){
		ArrayList<String> moves = new ArrayList<>();
		if(solvedBoard(board)){
			// solve board
			return moves;
		}

		// Not Solved
		// Initialize initial board state
		int heuristic_cost = manhattan(board);
		int move_cost = 0;
		BoardState initial = new BoardState(board,blankRow,blankCol,move_cost,heuristic_cost,"",null);

		// set up priority queue to keep board states
		PriorityQueue<BoardState> states = new PriorityQueue<>();
		// initialize hashset to make sure duplicates are not put in queue
		HashSet<String> visited = new HashSet<>();
		states.add(initial);
		visited.add(initial.toString());

		// use A* to explore graph of puzzle states
		while (!states.isEmpty()){
			// get current state from queue
			BoardState current = states.poll();

			// check if current state is solved
			if(solvedBoard(current.board)){
				// solved -> return list of moves
				board = current.board;
				BoardState temp = current;
				while(temp.parent != null){
					moves.add(temp.moveBefore);
					temp = temp.parent;
				}
				System.out.println(current.cost);
				return moves;
			}

			// not solved, search all children and add to queue
			ArrayList<BoardState> children = generateChildren(current,visited);
			states.addAll(children);
		}

		return moves;
	}


	// HEURISTIC #1: MANHATTAN DISTANCE + LINEAR CONFLICT + HAMMING DISTANCE
	private static int manhattan(int[][] board){
		// Manhattan Distance
		int numOutOfOrder = 0;
		int counter = 1;
		int distance = 0;
		int size = board.length;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				// check not -1 -> ignore if case
				if(board[i][j] != -1) {
					// count out of order tiles
					if (counter != board[i][j]) {
						numOutOfOrder++;
					}

					if (board[i][j] != 0) {  // ignore the empty tile
						int goalRow = (board[i][j] - 1) / size;
						int goalCol = (board[i][j] - 1) % size;
						int dx = Math.abs(i - goalRow);
						int dy = Math.abs(j - goalCol);
						distance += dx + dy;
						if (dx == 0 && i != goalRow || dy == 0 && j != goalCol) {  // check for linear conflicts
							distance += 2;
						}
					}
					counter++;
				}
			}
		}
		return distance + numOutOfOrder;
	}

	// generate children of a given board-state
	private static ArrayList<BoardState> generateChildren(BoardState currentState, HashSet<String> visited){
		ArrayList<BoardState> children = new ArrayList<>();

		int[][] board = currentState.board;
		int blankRow = currentState.blankRow;
		int blankCol = currentState.blankCol;
//		int cost = currentState.cost + 1;
		int cost = 0;

		// generate all situations where blank tile moves

		// blank tile moves up
		if(blankRow > 0){
			// create copy of board
			int[][] newBoard = copyBoard(board);
			// keep track of move
			String move = board[blankRow-1][blankCol] + " D";
			// swap places of blank tile with one above
			swap(newBoard,blankRow,blankCol,blankRow - 1, blankCol);

			// calculate cost of newBoard
			int heuristic_cost = manhattan(newBoard);
			// add new state into children
			BoardState child = new BoardState(newBoard,blankRow-1,blankCol,cost,heuristic_cost,move,currentState);

			if (!visited.contains(child.toString())) {  // check for duplicates
				children.add(child);
				visited.add(child.toString());  // add to hash set
			}
		}

		// blank tile move down
		if(blankRow < SIZE-1){
			// create copy of board
			int[][] newBoard = copyBoard(board);
			// keep track of move
			String move = board[blankRow+1][blankCol] + " U";
			// swap places of blank tile with one below
			swap(newBoard,blankRow,blankCol,blankRow + 1, blankCol);

			// calculate cost of newBoard
			int heuristic_cost = manhattan(newBoard);
			// add new state into children
			BoardState child = new BoardState(newBoard,blankRow+1,blankCol,cost,heuristic_cost,move,currentState);
			if (!visited.contains(child.toString())) {  // check for duplicates
				children.add(child);
				visited.add(child.toString());  // add to hash set
			}
		}

		// blank tile move right
		if(blankCol < SIZE - 1){
			// create copy of board
			int[][] newBoard = copyBoard(board);
			// keep track of move
			String move = board[blankRow][blankCol+1] + " L";
			// swap places of blank tile with one left
			swap(newBoard,blankRow,blankCol,blankRow, blankCol + 1);

			// calculate cost of newBoard
			int heuristic_cost = manhattan(newBoard);
			// add new state into children
			BoardState child = new BoardState(newBoard,blankRow,blankCol + 1,cost,heuristic_cost,move,currentState);
			if (!visited.contains(child.toString())) {  // check for duplicates
				children.add(child);
				visited.add(child.toString());  // add to hash set
			}
		}

		// blank tile move left
		if(blankCol > 0){
			// create copy of board
			int[][] newBoard = copyBoard(board);
			// keep track of move
			String move = board[blankRow][blankCol-1] + " R";
			// swap places of blank tile with one right
			swap(newBoard,blankRow,blankCol,blankRow, blankCol - 1);

			// calculate cost of newBoard
			int heuristic_cost = manhattan(newBoard);
			// add new state into children
			BoardState child = new BoardState(newBoard,blankRow,blankCol - 1,cost,heuristic_cost,move,currentState);
			if (!visited.contains(child.toString())) {  // check for duplicates
				children.add(child);
				visited.add(child.toString());  // add to hash set
			}
		}

		return children;
	}

	// write solution to file
	public static void writeSol(ArrayList<String> moves, File sol) throws IOException {
		FileWriter fw = new FileWriter(sol);

		for(int i = moves.size() - 1; i > -1; i--){
			fw.write(moves.get(i) + "\n");
		}

		fw.close();
	}

	// Helper functions
	// check if board is solved
	private static boolean solvedBoard(int[][] board){
		int value = 1;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (i == board.length - 1 && j == board[i].length - 1) {
					// The bottom-right corner should be empty in the solved state
					if (board[i][j] != 0) {
						return false;
					}
				} else {
					// All other tiles should be in ascending order from left to right, top to bottom
					if (board[i][j] != value) {
						return false;
					}
					value++;
				}
			}
		}
		return true;
	}


	// swap tiles
	// Helper method to swap two tiles on the board
	private static void swap(int[][] board, int x1, int y1, int x2, int y2) {
		int temp = board[x1][y1];
		board[x1][y1] = board[x2][y2];
		board[x2][y2] = temp;
	}
	private static int[][] copyBoard(int[][] board) {
		int size = board.length;
		int[][] newBoard = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				newBoard[i][j] = board[i][j];
			}
		}
		return newBoard;
	}

	// print board for debugging
	private static void printBoard(int[][] board){
		int size = SIZE;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				System.out.print(board[i][j] + " ");
			}
			System.out.println();
		}

//		System.out.println("ROW: " + blankRow + " COL: " + blankCol);
	}

	// Main Method
	public static void main(String[] args) {
//		System.out.println("number of arguments: " + args.length);
//		for (int i = 0; i < args.length; i++) {
//			System.out.println(args[i]);
//		}

		if (args.length < 2) {
			System.out.println("File names are not specified");
			System.out.println("usage: java " + MethodHandles.lookup().lookupClass().getName() + " input_file output_file");
			return;
		}
		
		// TODO
		File input = new File(args[0]);
		ReadBoard(input);

		printBoard(board);
		ArrayList<String> solution = solveBoard();
		// solve...
		File output = new File(args[1]);

		try {
			writeSol(solution, output);
		} catch (Exception e){
			// TODO handle exception
		}

		printBoard(board);

	}
}
