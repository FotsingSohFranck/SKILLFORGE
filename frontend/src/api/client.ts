import { useAuth } from '../auth/authStore'
import { ApiError, type ApiResponse } from './types'

const BASE = '/api/v1'

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

function unwrap<T>(res: Response, json: ApiResponse<T>): T {
  if (!res.ok || json.error) {
    throw new ApiError(
      json.error ?? { code: 'HTTP_' + res.status, message: res.statusText, details: [] },
    )
  }
  return json.data as T
}

/** Analyse la réponse : gère le 204 sans corps (undefined) et l'enveloppe standard. */
async function parse<T>(res: Response): Promise<T> {
  if (res.status === 204) {
    if (!res.ok) {
      throw new ApiError({ code: 'HTTP_' + res.status, message: res.statusText, details: [] })
    }
    return undefined as T
  }
  return unwrap<T>(res, (await res.json()) as ApiResponse<T>)
}

async function send(method: Method, path: string, body: unknown, token: string | null): Promise<Response> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  return fetch(BASE + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

/** Requête sans authentification (login / register / refresh / logout). */
export async function rawRequest<T>(method: Method, path: string, body?: unknown): Promise<T> {
  const res = await send(method, path, body, null)
  return parse<T>(res)
}

/** Requête authentifiée : ajoute le Bearer et tente un refresh unique sur 401. */
export async function apiFetch<T>(method: Method, path: string, body?: unknown): Promise<T> {
  let res = await send(method, path, body, useAuth.getState().accessToken)
  if (res.status === 401) {
    const refreshed = await useAuth.getState().refresh()
    if (refreshed) {
      res = await send(method, path, body, useAuth.getState().accessToken)
    }
  }
  return parse<T>(res)
}
