package fr.thipow.undercover.game.managers;

import fr.thipow.undercover.game.EWords;
import fr.thipow.undercover.game.GamePlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages the selection and assignment of words for players in the Undercover game. This class handles the generation
 * of random words from the EWords enum and assigns them to players based on their roles. It also provides methods to
 * reset the words for a new game.
 *
 * @author Thipow
 */

public class WordManager {

    private String civilsWord;
    private String undercoverWord;

    /**
     * Select random words for the game from the EWords enum.
     */
    private void generateWords() {
        EWords[] values = EWords.values();
        EWords selected = values[new Random().nextInt(values.length)];
        civilsWord = selected.getWord1();
        undercoverWord = selected.getWord2();
    }

    /**
     * Assign words to players based on their roles.
     */
    public void assignWords(List<GamePlayer> originalPlayers) {
        generateWords();
        List<GamePlayer> players = new ArrayList<>(originalPlayers);
        Collections.shuffle(players);

        for (GamePlayer player : players) {
            switch (player.getRole()) {
                case UNDERCOVER -> player.setWord(undercoverWord);
                case CIVIL -> player.setWord(civilsWord);
                case MR_WHITE -> player.setWord(""); // Mr White n’a pas de mot
            }
        }
    }

    /**
     * Gets the word assigned to civil players.
     *
     * @return The word for civil players.
     */
    public String getCivilsWord() {
        return civilsWord;
    }

    /**
     * Gets the word assigned to the undercover player.
     *
     * @return The word for the undercover player.
     */
    public String getUndercoverWord() {
        return undercoverWord;
    }

    /**
     * Resets the words to null, allowing for a new selection in the next game.
     */
    public void reset() {
        civilsWord = null;
        undercoverWord = null;
    }
}
