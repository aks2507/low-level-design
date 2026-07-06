import constants.DiscColor;

import java.lang.reflect.Array;

public class Board {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private final DiscColor[][] grid;

    private static final int[][] DIRECTIONS = new int[][]{
            {0, 1},
            {1, 0},
            {1, -1},
            {-1, 1}
    };

    public Board() {
        this.grid = new DiscColor[ROWS][COLUMNS];
    }

    private int getColumns() {
        return COLUMNS;
    }

    private int getRows() {
        return ROWS;
    }

    private boolean isValidCell(int row, int column) {
        return row >= 0 && row < ROWS && column >= 0 && column < COLUMNS;
    }

    public int placeDisc(int column, DiscColor discColor) {
        if (!canPlace(column)) {
            return -1;
        }

        for(int i = ROWS - 1; i >= 0; i--) {
            if (grid[i][column] == null) {
                grid[i][column] = discColor;
                break;
            }
        }

        return -1;
    }

    public boolean isFull(Board board) {
        for (int i = 0; i < COLUMNS; i++) {
            if (grid[0][i] == null) {
                return false;
            }
        }
        return true;
    }

    public boolean canPlace(int column) {
        if (column < 0 || column >= COLUMNS) {
            return false;
        }
        return grid[0][COLUMNS] == null;
    }

    public DiscColor getCell(int row, int column) {
        if (isValidCell(row, column)) {
            return grid[row][column];
        }
        return null;
    }

    public boolean checkWin(int row, int column, DiscColor color) {
        if (!isValidCell(row, column)) {
            return false;
        }

        if (grid[row][column] != color) {
            return false;
        }

        for (int[] direction: DIRECTIONS) {
            int count = 1;
            count += checkInDirection(row, column, direction[0], direction[1], color);
            count += checkInDirection(row, column, -direction[0], -direction[1], color);

            if (count >= 4) {
                return true;
            }
        }

        return false;
    }

    private int checkInDirection(int row, int column, int directionRow, int directionColumn, DiscColor color) {
        int count = 0;

        int nextRow = row + directionRow;
        int nextColumn = column + directionColumn;

        while(isValidCell(nextRow, nextColumn) && grid[nextRow][nextColumn] == color) {
            count++;
            nextRow += directionRow;
            nextColumn += directionColumn;
        }

        return count;
    }
}
