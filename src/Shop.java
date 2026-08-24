import java.util.Scanner;

// ==========================================================
// SHOP
// ==========================================================
// Shop doesn't own coins or potions itself — it borrows
// access to a Player object and calls that Player's own
// PUBLIC methods (spendCoins, addPotion, upgradeSword) to
// change it. Shop never touches Player's private/protected
// data directly — same encapsulation rule as everywhere else.
// ==========================================================

public class Shop {

    public static void enter(Scanner input, Player player) {
        System.out.println("You have entered the shop! Type 'Leave' anytime.");

        boolean shopping = true;
        while (shopping) {
            System.out.println();
            System.out.println("You have " + player.getCoins() + " coins.");
            System.out.println("1. Health Potion (10 coins)");
            System.out.println("2. Upgrade Sword (+3 damage, 25 coins)");
            System.out.println("3. Leave");

            String choice = input.nextLine();

            if (choice.equalsIgnoreCase("1") || choice.equalsIgnoreCase("Health Potion")) {
                if (player.spendCoins(10)) {
                    player.addPotion();
                    System.out.println("Bought a potion! You now have " + player.getPotions() + ".");
                }
            } else if (choice.equalsIgnoreCase("2") || choice.equalsIgnoreCase("Upgrade Sword")) {
                if (player.spendCoins(25)) {
                    player.upgradeSword(3);
                    System.out.println("Sword upgraded! Bonus damage: " + player.getSwordDamage());
                }
            } else if (choice.equalsIgnoreCase("3") || choice.equalsIgnoreCase("Leave")) {
                System.out.println("You left the shop.");
                shopping = false;
            } else {
                System.out.println("Not a valid shop option.");
            }
        }
    }
}