using System.Collections.Generic;

/// <summary>
/// A GameView is the data sent from the server representing our view of the game-- 
/// what data we're aware of.
/// </summary>
[System.Serializable]
public class GameView {

    /// <summary>
    /// The viewer is the player who this GameView is for, which should
    /// be our player.
    /// </summary>
    public string viewer;
    /// <summary>
    /// Which position are we in? 0 is first player, 1 is second player.
    /// </summary>
    public int viewerPosition;
    public long gameId;
    public int turn;
    /// <summary>
    /// Whose turn is it?
    /// </summary>
    public string currentPlayer;
    public List<PlayerView> players;
    public List<EntityView> inPlay;
    public List<EntityCoordinates> coordinates;

    public PlayerView getPlayerView(string playerName)
    {
        foreach(PlayerView pv in players)
        {
            if (pv.playerName == playerName) return pv;
        }
        return null;
    }

    public PlayerView GetMyPlayerView()
    {
        return players[viewerPosition - 1];
    }

    public PlayerView GetOpponentView()
    {
        if(viewerPosition == 2)
        {
            return players[1];
        }
        else
        {
            return players[0];
        }
    }

}
