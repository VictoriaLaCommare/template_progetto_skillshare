package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.AbstractRemoteServiceServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
//TEST DELLA SERVLET
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RegistrazioneImpTest {

    // Configurazione standard per testare una GWT Servlet come dall'esempio
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;
    @Mock private HttpServletRequest request;

    private RegistrazioneImp registratore;
    private Utente uservalido;
    private Utente userNONvalido;

@BeforeAll
    static void testmode(){
        DatabaseCore.enableTestMode();
    }

    @BeforeEach
    void setUp() throws Exception {
        DatabaseUtente.resetPerTest();
        // Inizializzazione fittizio del Web Server
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getServerInfo()).thenReturn("MockServer/1.0");
        when(request.getHeader("User-Agent")).thenReturn("MockBrowser/1.0");

        registratore = new RegistrazioneImp();
        registratore.init(servletConfig);

        // Crea un ThreadLocal con la request mock e iniettalo via reflection
        ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();
        threadLocal.set(request);

        Field field = AbstractRemoteServiceServlet.class.getDeclaredField("perThreadRequest");
        field.setAccessible(true);
        field.set(registratore, threadLocal);

        uservalido = new Utente("Mario","Rossi");
        uservalido.setUsername("mario123"+ System.currentTimeMillis());
        uservalido.setPassword("passwordSicura123" );
        uservalido.setMail("mario@email.com." + System.currentTimeMillis());
        uservalido.setData(new java.util.Date());

        userNONvalido = new Utente("","b");
    }
    @AfterAll
    static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }


    //TEST DI ACCETTAZIONE 1
    @Test
    void RegistraUtente_ConDatiCorretti_DeveTornareTrue(){
        try {
            assertTrue(registratore.registraUtente(uservalido));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TEST DI ACCETTAZIONE 2
    @Test
    void RegistraUtente_ConDatiNONCorretti_DeveLanciareIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () ->registratore.registraUtente(userNONvalido));

    }

    //TEST DI ACCETTAZIONE 3
    @Test
    public void registraUtente_UnUtenteGiaRegistratoConUnaMail_LanciaClassNotFoundExc() throws Exception {
    registratore.registraUtente(uservalido);
        assertThrows(Exception.class,
                () -> registratore.registraUtente(uservalido));
    }

    //TEST LOGIN
    @Test
    @DisplayName("Scenario 1: Login effettuato con successo")
    void effettuaLogin_ConCredenzialiCorrette_RitornaUtente() throws Exception {
        // Prima registriamo l'utente valido
        registratore.registraUtente(uservalido);

        // Tentiamo il login con mail e password corrette
        Utente utenteLoggato = registratore.effettuaLogin(uservalido.getMail(), "passwordSicura123");

        // Assert: L'utente viene autenticato correttamente
        assertNotNull(utenteLoggato, "L'utente loggato non deve essere null");
        assertEquals(uservalido.getMail(), utenteLoggato.getMail());
    }

    @Test
    @DisplayName("Scenario 2: Fallimento per credenziali errate")
    void effettuaLogin_ConPasswordErrata_RitornaNullOEccezione() throws Exception {
        // Arrange: Registriamo l'utente
        registratore.registraUtente(uservalido);

        // Verifichiamo che con una password errata il login fallisca (restituendo null o gestendo l'errore)
        Utente utenteLoggato = registratore.effettuaLogin(uservalido.getMail(), "passwordSbagliata");
        assertNull(utenteLoggato, "Il login con password errata deve restituire null");
    }

    @Test
    @DisplayName("Scenario 3: Fallimento per campi vuoti o mancanti")
    void effettuaLogin_ConCampiVuoti_LanciaEccezione() {
        // Campi vuoti devono impedire l'accesso lanciando un'eccezione di validazione
        assertThrows(IllegalArgumentException.class,
                () -> registratore.effettuaLogin("", ""));
    }

}