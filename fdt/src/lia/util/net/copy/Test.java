package lia.util.net.copy;

/**
 * Mehdi AKbarian-astaghi 10/7/19
 */
public class Test {

    public static void main(String[] args) throws Exception {

        args = new String[]{"-c","192.168.6.28","-p","54321", "-d", "/usr/sender", "/home/mehdi/Desktop/arvin-fdt/arvin.arvin"};
        //System.out.println(args);
        FDTMain.main(args);
    }
}
