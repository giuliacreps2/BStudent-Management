package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

// Indirizzi delle pagine (materie, corsi, missioni, Risorse): stesso formato dei CHECK della v0.9
public final class Slugs {

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern NOT_ALLOWED = Pattern.compile("[^a-z0-9]+");
    private static final int MAX_LENGTH = 120;

    private Slugs() {
    }

    // "L'ablativo assoluto" -> "l-ablativo-assoluto"
    public static String from(String text) {
        String plain = ACCENTS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        String slug = NOT_ALLOWED.matcher(plain.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.isEmpty() ? "x" : slug;
    }

    // Lo slug indicato dall'Admin, se c'è, altrimenti ricavato dal titolo
    public static String resolve(String requested, String fallbackText) {
        if (requested == null || requested.isBlank()) {
            return from(fallbackText);
        }
        String slug = requested.strip();
        if (!FORMAT.matcher(slug).matches()) {
            throw new BadRequestException(
                    "L'indirizzo della pagina può contenere solo lettere minuscole, numeri e trattini (es. prima-declinazione)");
        }
        return slug;
    }
}
