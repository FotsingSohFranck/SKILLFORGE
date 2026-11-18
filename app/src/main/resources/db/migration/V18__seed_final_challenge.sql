-- Seed de démo : un Final Challenge explorable (SF-92). Rattaché au nœud "caching" du seed
-- (V8), déjà pourvu de leçons, pour que `docker compose up` donne une mission immédiatement
-- jouable. Le nœud est marqué `requires_challenge` afin d'illustrer le gating de fin de nœud.
--
-- La définition est insérée telle quelle en jsonb : c'est le même modèle (record ScenarioDefinition)
-- qui sert à l'import, au stockage et à la lecture. Scénario déjà valide (cf. content/challenge).

UPDATE nodes SET requires_challenge = true
WHERE id = '01920000-0000-7000-8000-0000000000d4';

INSERT INTO challenge_scenarios(id, scenario_key, node_id, title) VALUES
    ('01920000-0000-7000-8000-00000000c001', 'backend-caching-resilience',
     '01920000-0000-7000-8000-0000000000d4', 'Tenir la charge sans servir de données périmées')
ON CONFLICT (id) DO NOTHING;

INSERT INTO challenge_scenario_versions(id, scenario_id, version, status, definition, published_at) VALUES
    ('01920000-0000-7000-8000-00000000c002', '01920000-0000-7000-8000-00000000c001', 1, 'PUBLISHED',
$challenge$
{
  "scenarioKey": "backend-caching-resilience",
  "nodeId": "01920000-0000-7000-8000-0000000000d4",
  "title": "Tenir la charge sans servir de données périmées",
  "objective": "Rétablir des temps de réponse acceptables sur le service \"catalogue\" sans compromettre la fraîcheur des données ni la stabilité.",
  "briefing": "Tu es ingénieur backend dans une start-up. Le service \"catalogue\" ralentit aux heures de pointe (p95 ~1,8 s, base à 90 % CPU). Objectif : restaurer la performance en maîtrisant les risques. Contraintes : budget mémoire limité, pas de refonte lourde, pas de régression de fraîcheur des données.",
  "competencies": ["caching-strategy", "cache-invalidation", "risk-management"],
  "competencyWeights": { "caching-strategy": 40, "cache-invalidation": 35, "risk-management": 25 },
  "passThreshold": 80,
  "renforceThreshold": 60,
  "initialNodeId": "choose-strategy",
  "mode": "FORMATIF",
  "criticalConditions": [
    {
      "id": "served-corrupt-data",
      "description": "Des données périmées ont été servies aux utilisateurs sans détection ni correction.",
      "whenState": "dataIntegrity < 0"
    }
  ],
  "nodes": [
    {
      "id": "choose-strategy",
      "title": "Choisir une approche face au ralentissement",
      "context": "Le pic de 18 h arrive dans 10 minutes. La requête la plus chaude est une lecture répétée du catalogue.",
      "event": "Comment réduire la latence à temps ?",
      "interactionType": "SINGLE_CHOICE",
      "competencies": ["caching-strategy", "risk-management"],
      "choices": [
        {
          "id": "lru-cache",
          "label": "Mettre un cache applicatif LRU devant la requête chaude (avec TTL à définir)",
          "competencyScores": { "caching-strategy": 40, "risk-management": 10 },
          "effects": [
            { "variable": "latency", "op": "DECREASE", "value": 2 },
            { "variable": "memory", "op": "INCREASE", "value": 1 }
          ],
          "rationale": "Cible la lecture chaude, gain immédiat, risque maîtrisé si le TTL est bien réglé."
        },
        {
          "id": "add-index",
          "label": "Ajouter un index en base sur la colonne filtrée",
          "competencyScores": { "caching-strategy": 20, "risk-management": 10 },
          "effects": [ { "variable": "latency", "op": "DECREASE", "value": 1 } ],
          "rationale": "Utile mais plus lent à poser et n'absorbe pas la répétition des lectures identiques."
        },
        {
          "id": "cache-everything-no-ttl",
          "label": "Tout mettre en cache, sans TTL, pour aller au plus vite",
          "competencyScores": { "caching-strategy": 15, "risk-management": 0 },
          "effects": [
            { "variable": "latency", "op": "DECREASE", "value": 2 },
            { "variable": "staleRisk", "op": "INCREASE", "value": 3 }
          ],
          "rationale": "Gain de latence mais fort risque de servir des données périmées."
        }
      ],
      "transitions": [
        { "whenState": "default", "nextNodeId": "cache-config" }
      ],
      "terminal": false
    },
    {
      "id": "cache-config",
      "title": "Régler le cache",
      "context": "Le cache est en place. Il faut décider de sa politique d'expiration et de taille.",
      "event": "Quelle politique appliquer ?",
      "interactionType": "SINGLE_CHOICE",
      "competencies": ["cache-invalidation", "risk-management"],
      "choices": [
        {
          "id": "ttl-and-eviction",
          "label": "TTL court + éviction LRU + taille maximale",
          "competencyScores": { "cache-invalidation": 35, "risk-management": 10 },
          "effects": [
            { "variable": "staleRisk", "op": "DECREASE", "value": 2 },
            { "variable": "memory", "op": "DECREASE", "value": 1 }
          ],
          "rationale": "Fraîcheur bornée et mémoire maîtrisée."
        },
        {
          "id": "max-size-only",
          "label": "Seulement une taille maximale, sans TTL",
          "competencyScores": { "cache-invalidation": 15 },
          "effects": [ { "variable": "staleRisk", "op": "INCREASE", "value": 1 } ],
          "rationale": "Protège la mémoire mais laisse vieillir les données."
        },
        {
          "id": "no-limit",
          "label": "Aucune limite : laisser le cache grossir",
          "competencyScores": { "cache-invalidation": 0 },
          "effects": [
            { "variable": "memory", "op": "INCREASE", "value": 2 },
            { "variable": "staleRisk", "op": "INCREASE", "value": 1 }
          ],
          "rationale": "Risque de saturation mémoire et de données périmées."
        }
      ],
      "transitions": [
        { "whenState": "staleRisk >= 3", "nextNodeId": "stale-incident" },
        { "whenState": "default", "nextNodeId": "mission-success" }
      ],
      "terminal": false
    },
    {
      "id": "stale-incident",
      "title": "Incident : données périmées servies",
      "context": "Des utilisateurs signalent des prix obsolètes affichés depuis le cache.",
      "event": "Comment réagir ?",
      "interactionType": "DIAGNOSIS",
      "competencies": ["cache-invalidation", "risk-management"],
      "choices": [
        {
          "id": "invalidate-on-write",
          "label": "Invalider l'entrée de cache à chaque écriture de la donnée source",
          "competencyScores": { "cache-invalidation": 20, "risk-management": 15 },
          "effects": [ { "variable": "staleRisk", "op": "DECREASE", "value": 3 } ],
          "rationale": "Corrige la cause : la cohérence est rétablie à l'écriture."
        },
        {
          "id": "ignore",
          "label": "Ignorer : le cache finira bien par se vider",
          "competencyScores": { "cache-invalidation": 0, "risk-management": 0 },
          "effects": [ { "variable": "dataIntegrity", "op": "DECREASE", "value": 1 } ],
          "rationale": "Laisse servir des données fausses : atteinte à l'intégrité (éliminatoire)."
        }
      ],
      "transitions": [
        { "whenState": "dataIntegrity < 0", "nextNodeId": "mission-failed" },
        { "whenState": "default", "nextNodeId": "mission-success" }
      ],
      "terminal": false
    },
    {
      "id": "mission-success",
      "title": "Mission réussie",
      "context": "Le service tient la charge et sert des données fraîches.",
      "competencies": [], "choices": [], "transitions": [],
      "terminal": true
    },
    {
      "id": "mission-failed",
      "title": "Mission compromise",
      "context": "La performance est rétablie mais au prix de données erronées servies aux utilisateurs.",
      "competencies": [], "choices": [], "transitions": [],
      "terminal": true
    }
  ]
}$challenge$::jsonb, now())
ON CONFLICT (id) DO NOTHING;
