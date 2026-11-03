package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class GestoreAnnunciImpl extends RemoteServiceServlet implements GestoreAnnunci {

    //aggiunto pattern observer per gestire la corretta eliminazione degli annunci
    // Lista degli osservatori per gli eventi sugli annunci
    private static final List<AnnuncioObserver> osservatori = new ArrayList<>();

    public static void aggiungiOsservatore(AnnuncioObserver o) {
        osservatori.clear();
        osservatori.add(o);
    }

    private void notificaEliminazione(String idAnnuncio) {
        for (AnnuncioObserver o : osservatori) {
            o.dopoAnnuncioEliminato(idAnnuncio);
        }
    }

    // Metodo helper per estrarre l'accesso alla mappa annunci su MapDB
    private HTreeMap<String, Annuncio> getMappaAnnunci() {
        DB db = DatabaseCore.getDB();
        return (HTreeMap<String, Annuncio>) db.hashMap("annunci", Serializer.STRING, Serializer.JAVA).createOrOpen();
    }

    @Override
    public Boolean pubblicaAnnuncio(Annuncio annuncio) throws Exception {
        try {
            HTreeMap<String, Annuncio> mappaAnnunci = getMappaAnnunci();

            // Salviamo l'annuncio usando il suo ID univoco
            mappaAnnunci.put(annuncio.getId(), annuncio);

            // Salvare fisicamente le modifiche sul file del database
            DatabaseCore.commit();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Annuncio> ottieniTuttiGliAnnunci() throws Exception {
        List<Annuncio> listaAnnunci = new ArrayList<>();

        try {
            HTreeMap<String, Annuncio> mappaAnnunci = getMappaAnnunci();

            // Prendiamo tutti i valori dalla mappa e li mettiamo nella nostra lista
            listaAnnunci.addAll(mappaAnnunci.values());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaAnnunci;
    }

    @Override
    public List<Annuncio> ottieniAnnunciUtente(String username) throws Exception {
        List<Annuncio> listaAnnunci = new ArrayList<>();

        try {
            HTreeMap<String, Annuncio> mappaAnnunci = getMappaAnnunci();

            // Filtriamo solo gli annunci dell'utente richiesto
            for (Annuncio annuncio : mappaAnnunci.values()) {
                if (annuncio.getAutoreUsername().equals(username)) {
                    listaAnnunci.add(annuncio);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaAnnunci;
    }

    @Override
    public Boolean modificaAnnuncio(Annuncio annuncioModificato) throws Exception {
        try {
            HTreeMap<String, Annuncio> mappaAnnunci = getMappaAnnunci();

            if (!mappaAnnunci.containsKey(annuncioModificato.getId())) {
                return false;
            }

            Annuncio annuncioSicuro = mappaAnnunci.get(annuncioModificato.getId());

            // SICUREZZA: Blocchiamo la modifica se non sei l'autore originale
            if (!annuncioSicuro.getAutoreUsername().equals(annuncioModificato.getAutoreUsername())) {
                return false;
            }

            // Aggiorniamo solo i campi modificabili, ID e Autore restano protetti
            annuncioSicuro.setTitolo(annuncioModificato.getTitolo());
            annuncioSicuro.setDescrizione(annuncioModificato.getDescrizione());
            annuncioSicuro.setCompetenzaOfferta(annuncioModificato.getCompetenzaOfferta());
            annuncioSicuro.setCompetenzaRichiesta(annuncioModificato.getCompetenzaRichiesta());

            mappaAnnunci.put(annuncioSicuro.getId(), annuncioSicuro);
            DatabaseCore.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean eliminaAnnuncio(String id, String username) throws Exception {
        try {
            HTreeMap<String, Annuncio> mappaAnnunci = getMappaAnnunci();

            if (!mappaAnnunci.containsKey(id)) {
                return false;
            }

            Annuncio annuncioSicuro = mappaAnnunci.get(id);

            // SICUREZZA: Blocchiamo l'eliminazione se non sei l'autore originale
            if (!annuncioSicuro.getAutoreUsername().equals(username)) {
                return false;
            }

            mappaAnnunci.remove(id);
            DatabaseCore.commit();

            // Notifica della cancellazione dell'annuncio a tutti gli osservatori registrati
            notificaEliminazione(id);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Annuncio> cercaAnnunci(String competenzaOfferta, String competenzaRichiesta, String keyword, String ordinamento) throws Exception {
        List<Annuncio> tuttiGliAnnunci = ottieniTuttiGliAnnunci();
        List<Annuncio> risultatiFiltrati = new ArrayList<>();

        for (Annuncio annuncio : tuttiGliAnnunci) {
            // Controllo filtro competenza offerta (se specificato)
            boolean matchOfferta = (competenzaOfferta == null || competenzaOfferta.trim().isEmpty() ||
                    (annuncio.getCompetenzaOfferta() != null &&
                            annuncio.getCompetenzaOfferta().toLowerCase().contains(competenzaOfferta.toLowerCase().trim())));

            // Controllo filtro competenza richiesta (se specificato)
            boolean matchRichiesta = (competenzaRichiesta == null || competenzaRichiesta.trim().isEmpty() ||
                    (annuncio.getCompetenzaRichiesta() != null &&
                            annuncio.getCompetenzaRichiesta().toLowerCase().contains(competenzaRichiesta.toLowerCase().trim())));

            // Filtro per Testo Libero (cerca nel titolo o nella descrizione)
            boolean matchKeyword = (keyword == null || keyword.trim().isEmpty() ||
                    (annuncio.getTitolo() != null && annuncio.getTitolo().toLowerCase().contains(keyword.toLowerCase().trim())) ||
                    (annuncio.getDescrizione() != null && annuncio.getDescrizione().toLowerCase().contains(keyword.toLowerCase().trim())));

            // Se entrambi i filtri rispecchiano i criteri, l'annuncio viene incluso nei risultati
            if (matchOfferta && matchRichiesta && matchKeyword) {
                risultatiFiltrati.add(annuncio);
            }
        }

        // Gestione dell'ordinamento (Sorting)
        if (ordinamento != null) {
            switch (ordinamento.toLowerCase()) {
                case "titolo":
                    risultatiFiltrati.sort((a1, a2) -> {
                        String t1 = a1.getTitolo() != null ? a1.getTitolo() : "";
                        String t2 = a2.getTitolo() != null ? a2.getTitolo() : "";
                        return t1.compareToIgnoreCase(t2);
                    });
                    break;
                default:
                    break;
            }
        }

        return risultatiFiltrati;
    }

    @Override
    public List<Annuncio> ottieniAnnunciPreferiti(String username) throws Exception {
        List<Annuncio> risultatiFiltrati = new ArrayList<>();

        try {
            DB db = DatabaseCore.getDB();

            // Recupero la mail dell'utente tramite l'Indice Secondario
            Utente utenteCorrente = DatabaseUtente.getUtenteByUsername(username);
            if (utenteCorrente == null) return risultatiFiltrati;
            String mailUtente = utenteCorrente.getMail();

            //Recupero i profili a cui ho lasciato il cuore
            HTreeMap<String, List<String>> mappaPreferiti = (HTreeMap<String, List<String>>) db.hashMap("preferenze_match", Serializer.STRING, Serializer.JAVA).createOrOpen();
            List<String> profiliCuore = mappaPreferiti.getOrDefault(mailUtente, new ArrayList<>());

            //Recupero i tag/servizi desiderati dalla Wishlist
            HTreeMap<String, List<ServizioRichiesto>> mappaWishlist = (HTreeMap<String, List<ServizioRichiesto>>) db.hashMap("wishlist_servizi", Serializer.STRING, Serializer.JAVA).createOrOpen();
            List<ServizioRichiesto> miaWishlist = mappaWishlist.getOrDefault(mailUtente, new ArrayList<>());

            // Estraggo solo i nomi delle competenze desiderate per un controllo più veloce
            List<String> competenzeDesiderate = new ArrayList<>();
            for (ServizioRichiesto s : miaWishlist) {
                if (s.getTagCompetenza() != null) {
                    competenzeDesiderate.add(s.getTagCompetenza().toLowerCase().trim());
                }
            }

            //Scorro gli annunci e filtro escludendo i miei stessi annunci
            List<Annuncio> tuttiGliAnnunci = ottieniTuttiGliAnnunci();

            for (Annuncio annuncio : tuttiGliAnnunci) {

                if (annuncio.getAutoreUsername().equals(username)) continue;//

                boolean autoreTraIPreferiti = profiliCuore.contains(annuncio.getAutoreUsername());

                boolean competenzaInWishlist = isCompetenzaInWishlist(annuncio, competenzeDesiderate);

                // Se l'autore ha il cuore OPPURE la competenza è in wishlist, lo aggiungo al feed
                if (autoreTraIPreferiti || competenzaInWishlist) {
                    risultatiFiltrati.add(annuncio);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return risultatiFiltrati;
    }

    private static boolean isCompetenzaInWishlist(Annuncio annuncio, List<String> competenzeDesiderate) {
        boolean competenzaInWishlist = false;
        if (annuncio.getCompetenzaOfferta() != null) {
            String compOfferta = annuncio.getCompetenzaOfferta().toLowerCase().trim();
            // Controllo se l'offerta dell'annuncio contiene o corrisponde a un tag in wishlist
            for (String desiderata : competenzeDesiderate) {
                if (compOfferta.contains(desiderata)) {
                    competenzaInWishlist = true;
                    break;
                }
            }
        }
        return competenzaInWishlist;
    }
}