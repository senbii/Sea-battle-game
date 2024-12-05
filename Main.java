import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<Player> players = new ArrayList<>();
    private static int rows = 7;
    private static int cols = 7;
    private static int[][] matrix = new int[rows][cols];
    private static int[][] shots = new int[rows][cols];
    private static Random random = new Random();
    private static int[][] blockSizes = { { 1, 3 }, { 1, 2 }, { 1, 2 }, { 1, 1 }, { 1, 1 }, { 1, 1 }, { 1, 1 } };

    public static void main(String[] args) {
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(System.in);
        boolean gameContinues = true;

        while (gameContinues) {

            initializeMatrix();

            for (int[] blockSize : blockSizes) {
                placeRandomBlock(blockSize[0], blockSize[1]);
            }

            System.out.print("Enter your name: ");
            String name = sc.nextLine();
            while (name.isBlank()) {
                System.out.print("You didn't enter name. Try again: ");
                name = sc.nextLine();
            }
            Player player = getPlayer(name);

            if (player == null) {
                player = new Player(name);
                players.add(player);
            }

            showEmptyField();
            int numberOfButllets = 30;

            while (numberOfButllets > 0) {
                System.out.println("Attempts left: " + numberOfButllets);
                int[] coordinates = null;

                while (coordinates == null) {
                    coordinates = getPlayerInput();
                }

                int row = coordinates[0];
                int col = coordinates[1];

                if (shots[row][col] == 0) {
                    processPlayerShot(row, col);

                    if (matrix[row][col] == 1) {
                        if (isShipSunk(row, col)) {
                            System.out.println("You've sunk a ship! \uD83D\uDCA5");
                            markSunkShip(row, col);
                        }
                    }

                    System.out.println();
                    showUpdatedField();

                    if (checkAllShipsSunk()) {
                        System.out.println("Congratulations! You've sunk all ships!");
                        player.incrementWins();
                        break;
                    }

                    numberOfButllets--;

                } else {
                    System.out.println("You've already shot at this position. Try again.");
                }
            }

            if (numberOfButllets == 0) {
                System.out.println("\nGame over! You've used all your attempts.");
                System.out.println("This is where the ships were located:");
                showField();
                player.incrementLosses();
            }

            while (true) {
                System.out.print("\nDo you want to play again? (yes/no): ");
                String answer = sc.nextLine();
                if (answer.equalsIgnoreCase("no")) {
                    gameContinues = false;
                    System.out.println("\nHere are the statistics of all players:");
                    displayLeaderboard();
                    System.out.println("\nThanks for playing!");
                    System.out.println("Goodbye!");
                    break;
                } else if (answer.equalsIgnoreCase("yes")) {
                    break;
                } else {
                    System.out.print("Try again!");
                }
            }
        }
    }

    public static void initializeMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = 0;
                shots[i][j] = 0;
            }
        }
    }

    public static void placeRandomBlock(int blockHeight, int blockWidth) {
        boolean placed = false;

        while (!placed) {
            boolean isHorizontal = random.nextBoolean();
            int blockH = isHorizontal ? blockHeight : blockWidth;
            int blockW = isHorizontal ? blockWidth : blockHeight;

            int randomRow = random.nextInt(rows);
            int randomCol = random.nextInt(cols);

            if (canPlaceBlock(randomRow, randomCol, blockH, blockW, isHorizontal)) {
                for (int i = 0; i < blockH; i++) {
                    for (int j = 0; j < blockW; j++) {
                        matrix[randomRow + i][randomCol + j] = 1;
                    }
                }
                placed = true;
            }
        }
    }

    public static boolean canPlaceBlock(int row, int col, int blockHeight, int blockWidth, boolean isHorizontal) {
        if (isHorizontal) {
            if (col + blockWidth > cols || row + blockHeight > rows) {
                return false;
            }
            for (int i = row - 1; i <= row + blockHeight; i++) {
                for (int j = col - 1; j <= col + blockWidth; j++) {
                    if (i >= 0 && i < rows && j >= 0 && j < cols && matrix[i][j] != 0) {
                        return false;
                    }
                }
            }
        } else {
            if (row + blockHeight > rows || col + blockWidth > cols) {
                return false;
            }
            for (int i = row - 1; i <= row + blockHeight; i++) {
                for (int j = col - 1; j <= col + blockWidth; j++) {
                    if (i >= 0 && i < rows && j >= 0 && j < cols && matrix[i][j] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void clearScreen() {
        System.out.println("\\033[H\\033[2J");
        System.out.flush();
    }

    public static void showField() {
        System.out.print("   ");
        for (char c = 'A'; c < 'A' + cols; c++) {
            System.out.print(c + "  ");
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 0) {
                    System.out.print("~ ");
                } else {
                    System.out.print("\uD83D\uDEE5\uFE0F ");
                }
            }
            System.out.println();
        }
    }

    public static void showEmptyField() {
        System.out.print("  ");
        for (char c = 'A'; c < 'A' + cols; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < cols; j++) {
                System.out.print("~ ");
            }
            System.out.println();
        }
    }

    public static int[] getPlayerInput() {
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the coordinates (for example, B3):");
        String input = sc.nextLine().toUpperCase();
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("Exiting the game...");
            System.exit(0);
        }

        if (input.length() < 2 || input.length() > 3) {
            System.out.println("Incorrect format. Try again.");
            return null;
        }

        char columnChar = input.charAt(0);
        String rowStr = input.substring(1);

        boolean isNumeric = true;
        for (int i = 0; i < rowStr.length(); i++) {
            if (!Character.isDigit(rowStr.charAt(i))) {
                isNumeric = false;
                break;
            }
        }

        if (!isNumeric) {
            System.out.println("Row must be a number. Try again.");
            return null;
        }

        int col = columnChar - 'A';
        int row = Integer.parseInt(rowStr) - 1;

        if (col < 0 || col > 6 || row < 0 || row > 6) {
            System.out.println("Coordinates are out of range. Try again.");
            return null;
        }
        return new int[] { row, col };
    }

    public static void processPlayerShot(int row, int col) {
        if (shots[row][col] != 0) {
            System.out.println("You've already shot at this position. Try again.");
        } else {
            if (matrix[row][col] == 1) {
                System.out.println("Hit! x");
                shots[row][col] = 2;
            } else {
                System.out.println("Missed. m");
                shots[row][col] = 1;
            }
        }
    }

    public static boolean isShipSunk(int row, int col) {
        if (isHorizontalShip(row, col)) {
            int startCol = col;
            while (startCol >= 0 && matrix[row][startCol] == 1) {
                if (shots[row][startCol] != 2) {
                    return false;
                }
                startCol--;
            }
            startCol = col + 1;
            while (startCol < cols && matrix[row][startCol] == 1) {
                if (shots[row][startCol] != 2) {
                    return false;
                }
                startCol++;
            }

        } else {
            int startRow = row;
            while (startRow >= 0 && matrix[startRow][col] == 1) {
                if (shots[startRow][col] != 2) {
                    return false;
                }
                startRow--;
            }
            startRow = row + 1;
            while (startRow < rows && matrix[startRow][col] == 1) {
                if (shots[startRow][col] != 2) {
                    return false;
                }
                startRow++;
            }
        }
        return true;
    }

    public static void markSunkShip(int row, int col) {
        if (isHorizontalShip(row, col)) {
            int startCol = col;
            while (startCol >= 0 && matrix[row][startCol] == 1) {
                shots[row][startCol] = 3;
                if (row + 1 < rows) {
                    shots[row + 1][startCol] = 1;
                }
                if (row - 1 >= 0) {
                    shots[row - 1][startCol] = 1;
                }
                startCol--;
            }
            if (startCol >= 0) {
                shots[row][startCol] = 1;
            }
            startCol = col + 1;
            while (startCol < cols && matrix[row][startCol] == 1) {
                shots[row][startCol] = 3;
                if (row + 1 < rows) {
                    shots[row + 1][startCol] = 1;
                }
                if (row - 1 >= 0) {
                    shots[row - 1][startCol] = 1;
                }
                startCol++;
            }
            if (startCol < cols) {
                shots[row][startCol] = 1;
            }
        } else {
            int startRow = row;
            while (startRow >= 0 && matrix[startRow][col] == 1) {
                shots[startRow][col] = 3;
                if (col + 1 < cols) {
                    shots[startRow][col + 1] = 1;
                }
                if (col - 1 >= 0) {
                    shots[startRow][col - 1] = 1;
                }
                startRow--;
            }
            if (startRow >= 0) {
                shots[startRow][col] = 1;
            }
            startRow = row + 1;
            while (startRow < rows && matrix[startRow][col] == 1) {
                shots[startRow][col] = 3;
                if (col + 1 < cols) {
                    shots[startRow][col + 1] = 1;
                }
                if (col - 1 >= 0) {
                    shots[startRow][col - 1] = 1;
                }
                startRow++;
            }
            if (startRow < rows) {
                shots[startRow][col] = 1;
            }
        }
    }

    public static boolean isHorizontalShip(int row, int col) {
        if (col > 0 && matrix[row][col - 1] == 1) {
            return true;
        }
        if (col < cols - 1 && matrix[row][col + 1] == 1) {
            return true;
        }
        return false;
    }

    public static void showUpdatedField() {
        System.out.print("  ");
        for (char c = 'A'; c < 'A' + cols; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < cols; j++) {
                if (shots[i][j] == 2) {
                    System.out.print("x ");
                } else if (shots[i][j] == 1) {
                    System.out.print("m ");
                } else if (shots[i][j] == 3) {
                    System.out.print("S ");
                } else {
                    System.out.print("~ ");
                }
            }
            System.out.println();
        }
    }

    public static boolean checkAllShipsSunk() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 1 && shots[i][j] != 3) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Player getPlayer(String name) {
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    static class Player {
        private String name;
        private int wins;
        private int losses;

        public Player(String name) {
            this.name = name;
            this.wins = 0;
            this.losses = 0;
        }

        public String getName() {
            return name;
        }

        public int getWins() {
            return wins;
        }

        public int getLosses() {
            return losses;
        }

        public void incrementWins() {
            wins++;
        }

        public void incrementLosses() {
            losses++;
        }
    }

    public static void sortPlayersByWins(Player[] players) {
        int n = players.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (players[j].getWins() < players[j + 1].getWins()) {
                    Player temp = players[j];
                    players[j] = players[j + 1];
                    players[j + 1] = temp;
                }
            }
        }
    }

    public static void displayLeaderboard() {
        Player[] playerArray = new Player[players.size()];
        players.toArray(playerArray);

        sortPlayersByWins(playerArray);

        for (Player p : playerArray) {
            System.out.println(p.getName() + " - Wins: " + p.getWins() + ", Losses: " + p.getLosses());
        }
    }
}