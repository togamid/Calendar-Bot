import java.util.Random;

public class Util {
    private static Random random = new Random();

    public static boolean rollDiceWithPercentage(Integer upperBound) {
        return random.nextInt(100) < upperBound;
    }

    public static Integer nextInt(Integer upperBound) {
        return random.nextInt(upperBound);
    }

    public static Integer fahrenheitToCelsius(Integer fahrenheit) {
        return (int) ((fahrenheit - 32)/1.8);
    }
}
