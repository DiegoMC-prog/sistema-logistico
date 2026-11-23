#!/usr/bin/env python3
"""
Valida el motor de reorden (RF-34) por fuera del sistema: recalcula EOQ, stock de seguridad, punto de reorden y
cantidad de los tres escenarios a partir de las entradas que el propio backend devuelve y los compara con sus resultados.

Uso (con el backend levantado sobre una base con historial, p. ej. la de seed_carga.sql):
    python scripts/seed/validar_eoq.py [usuario] [password]          # por defecto compras01 / Admin#2026
    API_BASE=http://localhost:8082/api/v1 python scripts/seed/validar_eoq.py

Las entradas (demanda, desviacion, lead time...) vienen redondeadas a 1-4 decimales, por eso se compara con tolerancia.
Para verificar tambien las entradas contra la base de datos hay consultas de ejemplo en seed_carga_verificacion.sql.
"""
import json
import math
import os
import sys
import urllib.request
from statistics import NormalDist

BASE = os.environ.get("API_BASE", "http://localhost:8080/api/v1")
DIAS_ANIO = 365


def http(metodo, ruta, cuerpo=None, token=None):
    r = urllib.request.Request(BASE + ruta, method=metodo,
                               data=json.dumps(cuerpo).encode() if cuerpo is not None else None)
    r.add_header("Content-Type", "application/json")
    if token:
        r.add_header("Authorization", "Bearer " + token)
    with urllib.request.urlopen(r) as resp:
        return json.loads(resp.read().decode() or "null")


def escenario(s, k):
    """Misma formula que CalculadoraReorden: k = +1 pesimista, 0 estandar, -1 optimista."""
    media, desv = s["demandaDiaria"], s["desviacionDemanda"]
    lt, desv_lt = s["leadTimeDias"], s["desviacionLeadTime"]
    z = NormalDist().inv_cdf(s["nivelServicio"])
    d = max(media + k * desv, 0)
    l = max(lt + k * desv_lt, 0)
    S, H = s["costoPedido"], s["costoAlmacenAnual"]
    eoq = 0 if d <= 0 or S <= 0 else math.sqrt(2 * d * DIAS_ANIO * S / H)
    ss = max(z * math.sqrt(l * desv * desv + d * d * desv_lt * desv_lt), 0)
    rop = d * l + ss + d * s["diasAnticipo"]
    posicion = s["stockActual"] + s["enTransito"]
    cantidad = rop + eoq - posicion if posicion <= rop else 0
    if cantidad > 0 and s["perecedero"] and s["vidaUtilDias"]:
        sobrante = max(s["stockActual"] - d * l, 0)
        cantidad = min(cantidad, max(d * s["vidaUtilDias"] - sobrante, 0))
    return eoq, ss, rop, math.ceil(cantidad - 1e-9)


def main():
    usuario = sys.argv[1] if len(sys.argv) > 1 else "compras01"
    clave = sys.argv[2] if len(sys.argv) > 2 else "Admin#2026"
    token = http("POST", "/auth/login", {"username": usuario, "password": clave})["accessToken"]
    sugerencias, pagina = [], 0
    while True:
        p = http("GET", f"/abastecimiento/sugerencias?page={pagina}&size=100", token=token)
        sugerencias += p["content"]
        pagina += 1
        if pagina >= p["totalPages"]:
            break

    por_estado = {}
    for s in sugerencias:
        por_estado[s["estado"]] = por_estado.get(s["estado"], 0) + 1
    print("Sugerencias:", len(sugerencias), por_estado)

    comparados = fallas = 0
    for s in sugerencias:
        if s["estado"] not in ("REORDENAR", "SIN_NECESIDAD"):
            continue
        for k, clave_esc in ((1, "pesimista"), (0, "estandar"), (-1, "optimista")):
            eoq, ss, rop, cant = escenario(s, k)
            r = s[clave_esc]
            comparados += 1
            ok = all(abs(a - b) <= max(0.02 * abs(b), 0.6)
                     for a, b in ((eoq, r["eoq"]), (ss, r["stockSeguridad"]), (rop, r["puntoReorden"])))
            ok = ok and abs(cant - r["cantidad"]) <= max(0.03 * r["cantidad"], 3)
            if not ok:
                fallas += 1
                print(f"  DIFIERE {s['producto']} [{clave_esc}] esperado={tuple(round(x, 1) for x in (eoq, ss, rop, cant))} "
                      f"sistema={r}")
    print(f"Escenarios comparados: {comparados}; discrepancias: {fallas}")
    sys.exit(1 if fallas else 0)


if __name__ == "__main__":
    main()
