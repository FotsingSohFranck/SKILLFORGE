/**
 * Contexte borné <b>Lab</b> : évaluation pratique d'un nœud par un conteneur éphémère
 * (l'apprenant réalise une épreuve exécutée, dont la réussite valide le nœud).
 *
 * <p>Cadre : {@code docs/adr/0008-lab-executable-evaluation-pratique.md} et épic SF-91.
 * Étape SF-91a : <b>domaine pur + ports</b> (modèle et contrats) et migration de schéma ;
 * l'orchestration Docker, la soumission/correction et l'UI arrivent dans SF-91b et suivantes.
 *
 * <p>Architecture hexagonale : le domaine ne dépend ni de Spring, ni de R2DBC, ni du web.
 */
package dev.skillforge.lab;
