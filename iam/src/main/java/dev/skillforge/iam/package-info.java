/**
 * Contexte borné <b>IAM</b> (identité, rôles, authentification).
 *
 * <p>Architecture hexagonale attendue (cf. docs/ARCHITECTURE.md §2) :
 * <ul>
 *   <li>{@code domain} — model, port.in, port.out, exception</li>
 *   <li>{@code application} — use cases (services)</li>
 *   <li>{@code adapter.in.web} — controllers WebFlux, DTOs, mappers</li>
 *   <li>{@code adapter.out.persistence} — entités R2DBC, repositories, adapters, mappers</li>
 * </ul>
 *
 * <p>TODO <b>SF-12</b> : implémenter register/login/refresh/logout/me + admin utilisateurs.
 */
package dev.skillforge.iam;
