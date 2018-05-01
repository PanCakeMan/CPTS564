import java.util.Random;

public class RandomSimulator {
  // This is for generate the probabilities to meet requirements
  //Using Random Seed
  public static boolean IsBreakDown(int n) {
    Random rand = new Random();
    double seed = rand.nextDouble() * (double) n;

    if (seed <= (double) n * 0.3) {
      return true;

    }

    return false;
  }

  public static boolean IsAccident(int n) {
    Random rand = new Random();
    double seed = rand.nextDouble() * (double) n;

    if (seed <= (double) n * 0.1) {
      return true;

    }

    return false;
  }

  public static int getTrafficCondition(int n) {
    Random rand = new Random();
    double seed = rand.nextDouble() * (double) n;

    if (seed <= (double) n * 0.1) {

      return 2;

    } else if (seed > (double) n * 0.1 && seed <= (double) n * 0.35) {

      return 1;

    } else {

      return 0;

    }

  }

}
