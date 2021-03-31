package donation.pet.util;

import java.util.Random;

public class TokenUtil {

    public static String makeToken() {
        Random ran = new Random();
        StringBuilder sb = new StringBuilder();

        do {
            int num = ran.nextInt(75) + 48;
            if ((num >= 48 && num <= 57)
                    || (num >= 65 && num <= 90)
                    || (num >= 97 && num <= 122)) {
                sb.append((char) num);
            }
        } while (sb.length() < 30);

        return sb.toString();
    }
}
