package performer;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.*;

public class Perform {

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

    private static void printBoard(int[][] board){
        int size = SIZE;
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }

//        System.out.println("ROW: " + blankRow + " COL: " + blankCol);
    }

    public static int[][] performMove(int[][] board, String direction) {
        int size = board.length;
        int[][] newBoard = new int[size][size];
        int blankRow = -1;
        int blankCol = -1;

        // Find the row and column of the blank tile
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) {
                    blankRow = i;
                    blankCol = j;
                    break;
                }
            }
        }

        // Copy the current board state to the new board
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newBoard[i][j] = board[i][j];
            }
        }

        // Perform the specified move
        switch (direction) {
            case "up":
                if (blankRow > 0) {
                    newBoard[blankRow][blankCol] = newBoard[blankRow - 1][blankCol];
                    newBoard[blankRow - 1][blankCol] = 0;
                }
                break;
            case "down":
                if (blankRow < size - 1) {
                    newBoard[blankRow][blankCol] = newBoard[blankRow + 1][blankCol];
                    newBoard[blankRow + 1][blankCol] = 0;
                }
                break;
            case "left":
                if (blankCol > 0) {
                    newBoard[blankRow][blankCol] = newBoard[blankRow][blankCol - 1];
                    newBoard[blankRow][blankCol - 1] = 0;
                }
                break;
            case "right":
                if (blankCol < size - 1) {
                    newBoard[blankRow][blankCol] = newBoard[blankRow][blankCol + 1];
                    newBoard[blankRow][blankCol + 1] = 0;
                }
                break;
            default:
                break;
        }

        return newBoard;
    }

    public static List<String> readMovesFromFile(File f) {
        List<String> moveList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(f);
            while (scanner.hasNextLine()) {
                String move = scanner.nextLine().split(" ")[1]; // extract only the direction part
                moveList.add(move);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return moveList;
    }

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
//        board = performMove(board,"up");
//        printBoard(board);

        // solve...
        File steps = new File(args[1]);
        List<String> moves = readMovesFromFile(steps);

        for(int i = 0; i < moves.size(); i++){
            // perform moves
            if(moves.get(i).equals("U")){
                board = performMove(board,"down");
            } else if(moves.get(i).equals("D")){
                board = performMove(board,"up");
            } else if(moves.get(i).equals("R")){
                board = performMove(board,"left");
            } else if(moves.get(i).equals("L")){
                board = performMove(board,"right");
            }
        }

        // all moves performed
        System.out.println("ALL MOVES PERFORMED: " + moves.size());
        printBoard(board);

    }
}
