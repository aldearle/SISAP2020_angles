

import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    private static final String USER_HOME = System.getProperty("user.home");
    public static final String DEFAULT_KEY_DIR = ".ssh";
    public static final String KEY_EXTENSION = ".pem";
    public static final String DEFAULT_PRIVATE_KEY_FILE = "private_key" + KEY_EXTENSION;
    public static final String DEFAULT_PUBLIC_KEY_FILE = "public_key" + KEY_EXTENSION;

    private static final Path USER_HOME_PATH = Paths.get(USER_HOME);
    private static final Path DEFAULT_KEY_PATH = USER_HOME_PATH.resolve(Paths.get(DEFAULT_KEY_DIR));
    private static final Path DEFAULT_PRIVATE_KEY_PATH = DEFAULT_KEY_PATH.resolve(Paths.get(DEFAULT_PRIVATE_KEY_FILE));
    private static final Path DEFAULT_PUBLIC_KEY_PATH = DEFAULT_KEY_PATH.resolve(Paths.get(DEFAULT_PUBLIC_KEY_FILE));

    public static void main(String[] args) {
        System.out.println( "user home: " + USER_HOME_PATH );
        System.out.println( "key path: " + DEFAULT_KEY_PATH );
        System.out.println( "private key path: " + DEFAULT_PRIVATE_KEY_PATH );
        System.out.println( "public key path: " + DEFAULT_PUBLIC_KEY_PATH );
    }
}
