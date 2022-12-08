package laserlads;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class Laser_Bcrypt {

    public static boolean password_match(String plain, String hash ){
        if (BCrypt.checkpw(plain, hash))
            return true;
        else
            return false;
    }

    public static String get_bcrypt_password(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

}
