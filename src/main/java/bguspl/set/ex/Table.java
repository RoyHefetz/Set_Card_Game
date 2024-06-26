package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     *  New fields we added
     */
    protected LinkedList<Integer> openSlots;// contains which spots are available to place a card
    protected int[][] playersTokens; // contains on which slots players have placed their tokens
    protected AtomicBoolean tableLock = new AtomicBoolean(false); // a lock to prevent multiple threads from accessing the table at the same time

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.openSlots = new LinkedList<Integer>();
        for (int i = 0; i < env.config.tableSize; i++) {
            openSlots.add(i);
        }
        playersTokens = new int[env.config.players][env.config.featureSize];
        for (int i = 0; i < env.config.players; i++) {
            for (int j = 0; j < env.config.featureSize; j++) {
                playersTokens[i][j] = -1;
            }
        }
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard) 
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

         // TODO implement
        cardToSlot[card] = slot;
        slotToCard[slot] = card;
        env.ui.placeCard(card, slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        // TODO implement
        int card = slotToCard[slot];
        slotToCard[slot] = null;
        cardToSlot[card] = null;
        removeSlotTokens(slot); //when a card is being removed, it's tokens being removed as well
        openSlots.addLast(slot); //the slot is now open so it should been added to the list
        env.ui.removeCard(slot);
    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        // TODO implement
        for (int i = 0; i < env.config.featureSize; i++) {
            if (playersTokens[player][i] == -1) {
                playersTokens[player][i] = slot;
                env.ui.placeToken(player, slot);
                break;
            }
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement
        for (int i = 0; i < env.config.featureSize; i++) {
            if (playersTokens[player][i] == slot) {
                playersTokens[player][i] = -1;
                env.ui.removeToken(player, slot);
                return true;
            }
        }
        return false;
    }

    public void removeAllTokens(){
        env.ui.removeTokens();
        for (int i = 0; i < playersTokens.length; i++){
            for (int j = 0; j < env.config.featureSize; j++){
                if (playersTokens[i][j] != -1){
                    playersTokens[i][j] = -1;
                }
            }
        }
    }

    //New Method
    public void removePlayerTokens(int player) { //removes a specific player's tokens
        for (int i = 0; i < env.config.featureSize; i++){
            if (playersTokens[player][i] != -1){
                env.ui.removeToken(player, playersTokens[player][i]);
                playersTokens[player][i] = -1;
            }
        }
    }

    //New Method
    public void removeSlotTokens(int slot) { //removes all the tokens from a specific slot
        env.ui.removeTokens(slot);
        for (int i = 0; i < playersTokens.length; i++){
            for (int j = 0; j < env.config.featureSize; j++){
                if (playersTokens[i][j] == slot){
                    playersTokens[i][j] = -1;
                }
            }
        }
        
    }
}
