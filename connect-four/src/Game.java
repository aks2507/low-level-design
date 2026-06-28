import constants.GameState;

public class Game {
    Board board;
    Player player1;
    Player player2;
    Player winner = null;
    Player currentPlayer;
    GameState gameState;

    public static final int INVALID_PLACEMENT = -1;

    // Constructor
    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public boolean MakeMove(Player player, int column) {
        if (gameState != GameState.IN_PROGRESS) {
            return false;
        }
        if (player != currentPlayer) {
            return false;
        }

        int row = board.placeDisc(column, player.discColor);
        if (row == INVALID_PLACEMENT) {
            return false;
        }

        if(board.checkWin(row, column, player.discColor)) {
            gameState = GameState.WIN;
            winner = player;
        } else if (board.isFull(board)) {
            gameState = GameState.DRAW;
        } else {
            currentPlayer = (player == player1) ? player2 : player1;
        }

        return true;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public Player getWinner() {
        return this.winner;
    }

    public Board getBoard() {
        return this.board;
    }
}
