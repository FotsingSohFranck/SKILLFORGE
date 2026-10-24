package forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests caches de l'epreuve « cache LRU borne ». Non modifiables par l'apprenant. */
class LruCacheTest {

    @Test
    @DisplayName("stocke et relit une valeur")
    void storesAndReads() {
        LruCache cache = new LruCache(2);
        cache.put("a", "1");
        assertEquals("1", cache.get("a"));
        assertEquals(1, cache.size());
    }

    @Test
    @DisplayName("renvoie null pour une cle absente")
    void missingKeyIsNull() {
        assertNull(new LruCache(2).get("x"));
    }

    @Test
    @DisplayName("evince la cle la moins recemment utilisee au-dela de la capacite")
    void evictsLeastRecentlyUsed() {
        LruCache cache = new LruCache(2);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3"); // depasse la capacite -> evince "a" (la plus ancienne)
        assertNull(cache.get("a"));
        assertEquals("2", cache.get("b"));
        assertEquals("3", cache.get("c"));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("un acces rafraichit la recence (get compte comme usage)")
    void accessRefreshesRecency() {
        LruCache cache = new LruCache(2);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.get("a");       // "a" redevient la plus recente
        cache.put("c", "3");  // doit evincer "b", pas "a"
        assertEquals("1", cache.get("a"));
        assertNull(cache.get("b"));
        assertEquals("3", cache.get("c"));
    }
}
