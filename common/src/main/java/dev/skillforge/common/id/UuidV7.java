package dev.skillforge.common.id;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * Génération d'UUID <b>v7</b> côté application (ordonnés dans le temps).
 * À n'utiliser que lorsqu'un identifiant doit être connu <em>avant</em> insertion ;
 * dans le cas nominal, la base génère l'ID via {@code DEFAULT uuidv7()}.
 * Ne jamais utiliser {@link UUID#randomUUID()} (v4) pour une clé.
 */
public final class UuidV7 {

    private UuidV7() {
    }

    /** @return un nouvel UUID version 7 (time-ordered epoch). */
    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
