package giuliacrepaldi.BStudent_Management.utilities;

import java.util.Locale;

public final class Emails {

    private Emails() {
    }

    // Forma salvata in email_normalized: confronti e unicità non dipendono da maiuscole o spazi
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
