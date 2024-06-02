package org.example.repository;

import org.example.model.Player;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class PlayerRepository extends AbstractRepository<Player, String> {

    public PlayerRepository() {
        super();
    }

    public boolean registerPlayer(String username, String password) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // Check if the player already exists
            TypedQuery<Player> query = entityManager.createQuery("SELECT p FROM Player p WHERE p.username = :username", Player.class);
            query.setParameter("username", username);
            List<Player> players = query.getResultList();

            if (!players.isEmpty()) {
                // Player already exists
                return false;
            }

            // Create new player
            Player newPlayer = new Player();
            newPlayer.setUsername(username);
            newPlayer.setPassword(password);
            newPlayer.setWins(0);

            create(newPlayer); // Using the create method from AbstractRepository
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            entityManager.close();
        }
    }

    public Player loginPlayer(String username, String password) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Player> query = entityManager.createQuery("SELECT p FROM Player p WHERE p.username = :username AND p.password = :password", Player.class);
            query.setParameter("username", username);
            query.setParameter("password", password);

            List<Player> players = query.getResultList();
            if (players.isEmpty()) {
                return null;
            } else {
                return players.get(0);
            }
        } finally {
            entityManager.close();
        }
    }

    public boolean updateWins(String username) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            TypedQuery<Player> query = entityManager.createQuery("SELECT p FROM Player p WHERE p.username = :username", Player.class);
            query.setParameter("username", username);

            Player player = query.getResultList().stream().findFirst().orElse(null);
            if (player == null) {
                return false;
            }

            player.setWins(player.getWins() + 1);
            entityManager.merge(player);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            entityManager.close();
        }
    }


}
