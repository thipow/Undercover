package fr.thipow.undercover.game.managers;

import fr.thipow.undercover.game.ERoles;
import fr.thipow.undercover.game.GamePlayer;
import fr.thipow.undercover.game.GameSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleManager {

    /**
     * Attribue les rôles aux joueurs en fonction des paramètres dans GameSettings.
     *
     * @param gamePlayers Liste des joueurs à qui assigner un rôle.
     */
    public void assignRoles(List<GamePlayer> gamePlayers) {
        int totalPlayers = gamePlayers.size();

        if (totalPlayers < 3) {
            throw new IllegalArgumentException("Au moins 3 joueurs sont nécessaires pour démarrer la partie.");
        }

        // Récupération des rôles à attribuer depuis GameSettings
        int undercoverCount = GameSettings.getUndercoverCount();
        int whiteCount = GameSettings.isWhiteEnabled() ? 1 : 0;

        int totalRolesCount = undercoverCount + whiteCount;

        if (totalRolesCount > totalPlayers) {
            throw new IllegalArgumentException("Le nombre total de rôles spéciaux dépasse le nombre de joueurs.");
        }

        List<ERoles> rolesToAssign = new ArrayList<>();

        // Ajout des rôles Undercover
        for (int i = 0; i < undercoverCount; i++) {
            rolesToAssign.add(ERoles.UNDERCOVER);
        }

        // Ajout des rôles Mr White si activé
        for (int i = 0; i < whiteCount; i++) {
            rolesToAssign.add(ERoles.MR_WHITE);
        }

        // Compléter avec des Civils pour arriver au total de joueurs
        int civilCount = totalPlayers - rolesToAssign.size();
        for (int i = 0; i < civilCount; i++) {
            rolesToAssign.add(ERoles.CIVIL);
        }

        // Mélanger la liste des rôles
        Collections.shuffle(rolesToAssign);

        // Attribution des rôles aux joueurs
        for (int i = 0; i < totalPlayers; i++) {
            gamePlayers.get(i).setRole(rolesToAssign.get(i));
        }
    }
}
