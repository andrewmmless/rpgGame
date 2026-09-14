import java.util.Random;
import java.util.Scanner;

// ==========================================================
// TRAINING GROUNDS
// ==========================================================
// A safe way to earn XP with zero risk of dying — meant for a
// fresh level 1 character to get a level or two under their
// belt before wandering into Whispering Woods half-dead.
//
// It's a Simon Says-style combo game: the trainer calls out a
// sequence of moves, it gets printed once, and you have to
// type it back in order. Each round you clear gets longer (and
// pays out more XP), and a mistake just ends the session — no
// health lost, ever. The sequence length itself is what caps
// how much free XP you can realistically farm here, since it
// gets genuinely hard to remember past round 5 or 6.
// ==========================================================

public class TrainingGrounds {

    private static final String[] MOVES = {"slash", "block", "dodge", "thrust"};

    public static void train(Scanner input, Random gen, Player player) {

        System.out.println();
        System.out.println("You step onto the training grounds.");
        System.out.println("The trainer calls out a combo — type it back, in order, separated by spaces.");
        System.out.println("Miss one and the session ends, but you keep every bit of XP already earned.");
        System.out.println("(Moves: slash, block, dodge, thrust)");

        int round = 3; // sequence starts at length 3
        boolean training = true;

        while (training) {

            String[] sequence = buildSequence(gen, round);

            System.out.println();
            System.out.println("Round " + (round - 2) + " — memorize this combo:");
            System.out.println(String.join(" ", sequence));
            System.out.print("Your turn: ");

            String response = input.nextLine();
            String[] attempt = response.trim().toLowerCase().split("\\s+");

            if (matches(sequence, attempt)) {

                int xpReward = round * 3;
                System.out.println("Nailed it!");
                player.gainXp(xpReward);

                round++;

                if (round > 8) {
                    System.out.println();
                    System.out.println("The trainer grins. \"That's enough for one day — go put it to use out there.\"");
                    training = false;
                } else {
                    System.out.println();
                    System.out.println("Keep training? (yes/no)");
                    String again = input.nextLine();
                    if (!again.equalsIgnoreCase("yes")) {
                        training = false;
                    }
                }

            } else {
                System.out.println("You fumbled the combo. Training session over.");
                training = false;
            }
        }

        System.out.println("You leave the training grounds.");
    }

    private static String[] buildSequence(Random gen, int length) {
        String[] sequence = new String[length];
        for (int i = 0; i < length; i++) {
            sequence[i] = MOVES[gen.nextInt(MOVES.length)];
        }
        return sequence;
    }

    private static boolean matches(String[] sequence, String[] attempt) {
        if (sequence.length != attempt.length) return false;

        for (int i = 0; i < sequence.length; i++) {
            if (!sequence[i].equalsIgnoreCase(attempt[i])) {
                return false;
            }
        }
        return true;
    }
}
