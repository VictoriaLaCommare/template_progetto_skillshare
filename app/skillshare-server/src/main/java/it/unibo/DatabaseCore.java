package it.unibo;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import java.io.File;

public class DatabaseCore {

    private static DB db;
    private static boolean testMode = Boolean.getBoolean("db.testmode"); //modifica necessaria per impedire i problemi con la serializzazione
    // Blocco statico di inizializzazione: apre il database una sola volta all'avvio della classe
    static {
        inizializzaDB();
    }

    private static synchronized void inizializzaDB() {
        if (db != null && !db.isClosed()) {
            return;
        }

        try {
            if (testMode) {
                db = DBMaker.memoryDB()
                        .transactionEnable()
                        .make();
            } else {
                String dataDir = System.getenv("DATA_DIR");
                String dbPath = "progetto_sweng.db";
                
                if (dataDir != null && !dataDir.trim().isEmpty()) {
                    File dir = new File(dataDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    dbPath = dataDir + (dataDir.endsWith(File.separator) ? "" : File.separator) + "progetto_sweng.db";
                    System.out.println("MAPDB -> Avvio in modalità CLOUD. Percorso: " + dbPath);
                } else {
                    System.out.println("MAPDB -> Avvio in modalità LOCALE.");
                }

                db = DBMaker.fileDB(dbPath)
                        .transactionEnable()
                        .fileLockDisable() // Disabilita il controllo dei lock di sistema
                        .closeOnJvmShutdown()
                        .make();
            }
        } catch (Exception e) {
            System.err.println("MAPDB -> Errore critico durante l'apertura del DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void enableTestMode() {
        testMode = true;
        close();
        inizializzaDB();
    }

    public static synchronized void disableTestMode() {
        testMode = false;
        close();
        inizializzaDB();
    }

    public static synchronized DB getDB() {
        if (db == null || db.isClosed()) {
            inizializzaDB();
        }
        return db;
    }

    public static synchronized void commit() {
        if (db != null && !db.isClosed()) {
            db.commit();
        }
    }

    public static synchronized void close() {
        try {
            if (db != null && !db.isClosed()) {
                db.close();
            }
        } catch (Exception e) {
            // Ignora eventuali eccezioni in chiusura
        }
        db = null;
    }
}