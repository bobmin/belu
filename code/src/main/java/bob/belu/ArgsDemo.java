package bob.belu;

import java.util.Arrays;

public class ArgsDemo {

    public static void main(String[] args) {
        System.out.println("ArgsDemo...");

        int idx = 0;
        for (String x: args) {
            System.out.println("[" + idx + "] " + x);
            if (x.matches("-[vV][aA][rR]=.*")) {
                String value = x.substring(5).replaceAll("`", "\"");
                System.out.println("\tvalue = \"" + value + "\" = " + Arrays.toString(value.getBytes()));
            }
            idx++;
        }

    }

}

