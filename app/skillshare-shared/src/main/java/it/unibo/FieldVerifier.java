package it.unibo;
import com.google.gwt.regexp.shared.RegExp;
import java.util.Date;
/**
 * <p>
 * FieldVerifier validates that the name the user enters is valid.
 * </p>
 * <p>
 * This class is in the <code>shared</code> project because we use it in both
 * the client code and on the server. On the client, we verify that the name is
 * valid before sending an RPC request so the user doesn't have to wait for a
 * network round trip to get feedback. On the server, we verify that the name is
 * correct to ensure that the input is correct regardless of where the RPC
 * originates.
 * </p>
 * <p>
 * When creating a class that is used on both the client and the server, be sure
 * that all code is translatable and does not use native JavaScript. Code that
 * is not translatable (such as code that interacts with a database or the file
 * system) cannot be compiled into client side JavaScript. Code that uses native
 * JavaScript (such as Widgets) cannot be run on the server.
 * </p>
 *
 * NOTA: java.util.regex.Pattern/Matcher e java.util.Calendar/GregorianCalendar
 * NON sono emulate da GWT lato client (nessuna configurazione di modulo può
 * abilitarle: sono classi del tutto assenti dalla JRE emulation). Per questo
 * qui sotto usiamo com.google.gwt.regexp.shared.RegExp al posto di Pattern,
 * e java.util.Date (che invece È emulata) al posto di GregorianCalendar.
 */
public class FieldVerifier {
	//matcher.match non è transcrivibile in JS per questo usiamo RegExp
	//pattern che contiene 3 caratteri alfabetici (ancorato ^...$ per riprodurre
	//la stessa semantica di "full match" che aveva Matcher.matches())
	private static final RegExp USERNAME_PATTERN =
			RegExp.compile("^(?:.*[a-z]){3}.*$", "i");
	@SuppressWarnings("deprecation")
	private static final Date ANNOMIN = new Date(0, 11, 31);
	@SuppressWarnings("deprecation")
	private static final Date ANNOMAX = new Date(126, 11, 31);

	/**
	 * Verifies that the specified name is valid for our service.
	 * In this example, we only require that the name is at least four
	 * characters. In your application, you can use more complex checks to ensure
	 * that usernames, passwords, email addresses, URLs, and other fields have the
	 * proper syntax.
	 *
	 * @param name the name to validate
	 * @return true if valid, false if invalid
	 */
	//
	public static boolean isValidName(String name) {
		//vale anche per il cognome
		if (name == null || name.isEmpty()) {
			return false;
		}
		return USERNAME_PATTERN.test(name) && name.length() >= 3;
	}

	public static boolean isValidUsername(String u){
		if(u==null || u.isEmpty())
			return false;
		//username deve contenere almeno tre caratteri alfabetici
		return USERNAME_PATTERN.test(u) && u.trim().length() >= 3 && u.trim().length() <= 25;
	}

	public static boolean isValidEmail(String mail) {
		if (mail == null || mail.isEmpty()) return false;
		return mail.contains("@") && mail.contains(".");
	}

	public static boolean isValidPassword(String password) {
		if (password == null || password.isEmpty()) return false;
		return password.trim().length() >= 8;
	}

	public static boolean isValidDataNascita(Date eta){
		if(eta==null) return false;
		return !eta.before(ANNOMIN) && !eta.after(ANNOMAX);
	}

	public static boolean isValidBio(String bio){
		return bio != null;
	}

	public static boolean isValidTag(String tag){
		return tag != null && !tag.isEmpty();
	}

	public static boolean isValidTitoloAnnuncio(String titolo) {
        return titolo != null && !titolo.trim().isEmpty() && titolo.length() <= 70;
    }

    public static boolean isValidDescrizioneAnnuncio(String descrizione) {
        return descrizione != null && !descrizione.trim().isEmpty() && descrizione.length() <= 200;
    }

    public static boolean isValidCompetenzaOfferta(String competenza) {
        return competenza != null && !competenza.trim().isEmpty() && competenza.length() <= 50;
    }

}
