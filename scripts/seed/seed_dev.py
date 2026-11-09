#!/usr/bin/env python3
"""
Datos de desarrollo para ver el frontend con información real (minimo 10 registros por tabla o flujo).

Se siembra a traves de la API, asi se respetan todas las reglas de negocio (stock FEFO, turnos, precios...).
Requiere la aplicacion levantada sobre una base RECIEN CREADA (con el esquema de V1 y el admin de la semilla).

Orden (ver README.md):
  1. python scripts/seed/seed_dev.py 1      catalogo, clientes, proveedores, cajas, roles, pedidos, recepciones, inventario
  2. ejecutar usuarios.sql                  usuarios con contrasena conocida (la API envia un correo por usuario)
  3. python scripts/seed/seed_dev.py 2      turnos, ventas, devoluciones, cambios, anulaciones, cierres de caja
  4. ejecutar fechas.sql                    reparte turnos y ventas en los ultimos 21 dias (para los reportes por fecha)

Para agregar a una base ya sembrada solo las ventas con precio manual o las reposiciones a costos mas altos:
seed_dev.py precios-manuales / seed_dev.py compras-variacion (y repetir fechas.sql).

Variables: API_BASE (por defecto http://localhost:8080/api/v1).
Contrasena de todos los usuarios sembrados, incluido admin al terminar: Admin#2026
"""
import datetime
import json
import os
import random
import sys
import uuid
import urllib.error
import urllib.request
from decimal import Decimal, ROUND_HALF_UP

BASE = os.environ.get("API_BASE", "http://localhost:8080/api/v1")
PASS_FINAL = "Admin#2026"          # contrasena de la semilla (V1) y de todos los usuarios sembrados
PASS_TEMPORAL = "Seed#Admin2026"   # la usa admin mientras corre el script (la semilla lo obliga a cambiarla)
random.seed(2026)

D = Decimal


def q2(x):
    return D(x).quantize(D("0.01"), rounding=ROUND_HALF_UP)


# ---------------------------------------------------------------------------------------------- HTTP

def req(metodo, ruta, cuerpo=None, token=None, cabeceras=None):
    r = urllib.request.Request(BASE + ruta, method=metodo,
                               data=json.dumps(cuerpo, default=str).encode() if cuerpo is not None else None)
    r.add_header("Content-Type", "application/json")
    if token:
        r.add_header("Authorization", "Bearer " + token)
    for k, v in (cabeceras or {}).items():
        r.add_header(k, v)
    try:
        with urllib.request.urlopen(r) as resp:
            status, texto = resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        status, texto = e.code, e.read().decode()
    try:
        datos = json.loads(texto) if texto else None
    except ValueError:
        datos = texto
    return status, datos


def ok(metodo, ruta, cuerpo=None, token=None, cabeceras=None):
    status, datos = req(metodo, ruta, cuerpo, token, cabeceras)
    if status >= 300:
        raise SystemExit(f"\nFALLO {metodo} {ruta} -> {status}: {str(datos)[:400]}\ncuerpo: {str(cuerpo)[:300]}")
    return datos


def intentar(metodo, ruta, cuerpo=None, token=None, cabeceras=None):
    """Como ok(), pero devuelve None si la API rechaza la operacion (para pasos opcionales)."""
    status, datos = req(metodo, ruta, cuerpo, token, cabeceras)
    return datos if status < 300 else None


def contenido(datos):
    return datos.get("content", datos) if isinstance(datos, dict) else datos


def login(usuario, password):
    status, d = req("POST", "/auth/login", {"username": usuario, "password": password})
    return d["accessToken"] if status == 200 else None


def paso(texto):
    print(f"\n== {texto}")


# ---------------------------------------------------------------------------------------------- catalogo

CATEGORIAS = ["Abarrotes", "Bebidas", "Lacteos", "Limpieza", "Panaderia", "Snacks", "Higiene", "Carnes frias",
              "Congelados", "Mascotas"]
UNIDADES = [("Unidad", "u"), ("Kilogramo", "kg"), ("Gramo", "g"), ("Litro", "l"), ("Mililitro", "ml"),
            ("Caja", "cj"), ("Paquete", "paq"), ("Docena", "doc"), ("Bolsa", "bls"), ("Botella", "bot")]
# (categoria, producto, precio, costo, perecedero)
PRODUCTOS = [
    ("Abarrotes", "Arroz 1kg", "7.50", "5.20", False), ("Abarrotes", "Azucar 1kg", "6.00", "4.10", False),
    ("Abarrotes", "Fideos 500g", "4.50", "3.00", False),
    ("Bebidas", "Gaseosa Cola 2L", "12.00", "8.50", False), ("Bebidas", "Agua Mineral 2L", "5.00", "3.20", False),
    ("Bebidas", "Jugo de Naranja 1L", "9.50", "6.40", True),
    ("Lacteos", "Leche Entera 1L", "8.00", "5.90", True), ("Lacteos", "Yogur Natural 1L", "11.00", "7.50", True),
    ("Lacteos", "Queso Fresco 500g", "28.00", "20.00", True),
    ("Limpieza", "Detergente 1kg", "22.00", "15.00", False), ("Limpieza", "Lavandina 1L", "6.50", "4.00", False),
    ("Limpieza", "Jabon de Barra", "3.50", "2.10", False),
    ("Panaderia", "Pan de Molde", "14.00", "9.50", True), ("Panaderia", "Galletas Dulces", "5.50", "3.40", True),
    ("Panaderia", "Tostadas", "7.00", "4.50", True),
    ("Snacks", "Papas Fritas 150g", "10.00", "6.50", False), ("Snacks", "Mani Salado 200g", "8.50", "5.50", False),
    ("Snacks", "Chocolate Barra", "6.00", "3.80", False),
    ("Higiene", "Shampoo 400ml", "25.00", "17.00", False), ("Higiene", "Pasta Dental", "13.50", "9.00", False),
    ("Higiene", "Papel Higienico x4", "16.00", "11.00", False),
    ("Carnes frias", "Jamon 250g", "18.00", "12.50", True), ("Carnes frias", "Salchicha x10", "20.00", "14.00", True),
    ("Carnes frias", "Mortadela 250g", "12.00", "8.00", True),
    ("Congelados", "Pollo Troceado 1kg", "24.00", "17.00", True), ("Congelados", "Helado Vainilla 1L", "21.00", "14.50", True),
    ("Congelados", "Papas Congeladas 1kg", "15.00", "10.00", True),
    ("Mascotas", "Alimento Perro 1kg", "26.00", "18.00", False), ("Mascotas", "Alimento Gato 1kg", "28.00", "19.50", False),
    ("Mascotas", "Arena para Gatos 4kg", "30.00", "21.00", False),
]
# Productos cuyo lote mas proximo a vencer lleva descuento (la venta los cobra siempre en efectivo en la semilla)
PRODUCTOS_CON_DESCUENTO = {"Yogur Natural 1L", "Pan de Molde", "Helado Vainilla 1L"}
# Productos con un minimo mayor que su stock, para que el reporte de stock bajo tenga filas
PRODUCTOS_BAJO_MINIMO = {"Arroz 1kg", "Leche Entera 1L", "Detergente 1kg", "Alimento Perro 1kg", "Pasta Dental"}
TIPOS_CLIENTE = ["MAYORISTA", "FRECUENTE", "VIP", "EMPLEADO", "EMPRESA", "ESTUDIANTE", "JUBILADO", "REVENDEDOR",
                 "INSTITUCION"]
METODOS_PAGO = ["QR", "CHEQUE", "BILLETERA MOVIL", "VALE", "PUNTOS", "GIRO"]
DEPOSITOS = ["Deposito Central", "Sucursal Norte", "Sucursal Sur", "Sucursal Este", "Sucursal Oeste", "Almacen Frio",
             "Deposito Mercado", "Deposito Terminal", "Deposito Aeropuerto", "Deposito Reserva"]
PROVEEDORES = [
    ("Distribuidora Andina", "Marcela Rojas"), ("Lacteos del Valle", "Pedro Quispe"), ("Panificadora La Espiga", "Rosa Vargas"),
    ("Bebidas del Sur", "Jorge Salinas"), ("Importadora Global", "Lucia Mendez"), ("Carnes Premium", "Raul Torrez"),
    ("Frigorifico Norte", "Silvia Choque"), ("Limpieza Total", "Mario Flores"), ("Higiene y Hogar", "Elena Castro"),
    ("Snacks Express", "Daniel Perez"), ("Mascotas Felices", "Paola Rios"), ("Abarrotes Mayoristas", "Hugo Aguilar"),
]
CLIENTES = [
    "Juan Perez", "Maria Lopez", "Carlos Gutierrez", "Ana Fernandez", "Luis Martinez", "Sofia Ramirez", "Diego Castillo",
    "Valeria Herrera", "Andres Morales", "Camila Ortiz", "Jorge Vargas", "Daniela Rojas", "Pablo Suarez", "Lucia Mendoza",
    "Fernando Rios", "Gabriela Soto", "Ricardo Paz", "Natalia Cruz", "Oscar Medina", "Patricia Vega",
]
CAJAS_DEPOSITO = [0, 0, 0, 0, 1, 1, 1, 2, 2, 3]   # Caja 1..10 -> indice de deposito

ROLES = {
    "CAJERO": ["CAJA_ABRIR", "CAJA_CERRAR", "CAJA_MOVIMIENTO", "VENTA_CREAR", "VENTA_DEVOLVER"],
    "SUPERVISOR": ["CAJA_ABRIR", "CAJA_CERRAR", "CAJA_MOVIMIENTO", "CAJA_SUPERVISAR", "VENTA_VER", "VENTA_CREAR",
                   "VENTA_ANULAR", "VENTA_DEVOLVER", "VENTA_PRECIO_MODIFICAR", "VENTA_PRECIO_BAJO_COSTO",
                   "REPORTE_VENTAS", "REPORTE_CAJA", "REPORTE_INVENTARIO", "INVENTARIO_VER", "CLIENTE_CREAR",
                   "CLIENTE_EDITAR"],
    "ENCARGADO_TURNO": ["CAJA_ABRIR", "CAJA_CERRAR", "CAJA_MOVIMIENTO", "CAJA_SUPERVISAR", "VENTA_CREAR", "VENTA_VER",
                        "VENTA_ANULAR", "VENTA_DEVOLVER"],
    "ALMACENERO": ["INVENTARIO_VER", "INVENTARIO_MOVER", "CATALOGO_CREAR", "CATALOGO_EDITAR", "DEPOSITO_CREAR",
                   "DEPOSITO_EDITAR", "PEDIDO_VER", "PEDIDO_RECIBIR", "REPORTE_INVENTARIO"],
    "COMPRADOR": ["REPORTE_COMPRAS", "PROVEEDOR_VER", "PROVEEDOR_CREAR", "PROVEEDOR_EDITAR", "PROVEEDOR_ACTIVAR", "PEDIDO_VER",
                  "PEDIDO_CREAR", "PEDIDO_EDITAR", "PEDIDO_ELIMINAR", "PEDIDO_ENVIAR", "PEDIDO_CANCELAR",
                  "PEDIDO_RECIBIR", "INVENTARIO_VER"],
    "GERENTE": ["VENTA_VER", "REPORTE_VENTAS", "REPORTE_CAJA", "REPORTE_INVENTARIO", "REPORTE_UTILIDAD",
                "REPORTE_COMPRAS", "INVENTARIO_VER", "PEDIDO_VER", "PROVEEDOR_VER", "USUARIO_VER",
                "ROL_VER", "CAJA_SUPERVISAR"],
    "AUDITOR": ["VENTA_VER", "REPORTE_VENTAS", "REPORTE_CAJA", "REPORTE_INVENTARIO", "REPORTE_UTILIDAD",
                "REPORTE_COMPRAS", "INVENTARIO_VER", "PEDIDO_VER", "PROVEEDOR_VER", "USUARIO_VER", "ROL_VER"],
    "VENDEDOR": ["VENTA_CREAR", "CLIENTE_CREAR", "CLIENTE_EDITAR"],
    "CONTADOR": ["VENTA_VER", "REPORTE_VENTAS", "REPORTE_CAJA", "REPORTE_UTILIDAD", "REPORTE_COMPRAS", "PEDIDO_VER", "PROVEEDOR_VER"],
}


def token_admin():
    t = login("admin", PASS_FINAL)
    if t:
        estado, _ = req("GET", "/permisos", token=t)
        if estado == 200:
            return t
        ok("POST", "/auth/cambiar-password", {"passwordActual": PASS_FINAL, "passwordNueva": PASS_TEMPORAL}, t)
    t = login("admin", PASS_TEMPORAL)
    if not t:
        raise SystemExit("No se pudo iniciar sesion como admin: la base debe ser nueva (admin / Admin#2026)")
    return t


# ---------------------------------------------------------------------------------------------- fase 1

def fase1():
    T = token_admin()
    if contenido(ok("GET", "/categorias?size=1", token=T)):
        raise SystemExit("La base ya tiene datos. Recreela (DROP/CREATE DATABASE) y vuelva a levantar la aplicacion.")

    paso("Roles y permisos")
    permisos = {p["nombre"]: p["id"] for p in contenido(ok("GET", "/permisos", token=T))}
    existentes = {r["nombre"]: r["id"] for r in contenido(ok("GET", "/roles?size=100", token=T))}
    for rol, nombres in ROLES.items():
        rol_id = existentes.get(rol) or ok("POST", "/roles", {"nombre": rol}, T)["id"]
        ok("PUT", f"/roles/{rol_id}/permisos", {"permisoIds": [permisos[n] for n in nombres]}, T)
    print(f"  {len(ROLES)} roles nuevos (mas ADMIN)")

    paso("Catalogo base")
    cat = {n: ok("POST", "/categorias", {"nombre": n}, T)["id"] for n in CATEGORIAS}
    uni = {n: ok("POST", "/unidades-medida", {"nombre": n, "abreviatura": a}, T)["id"] for n, a in UNIDADES}
    dep = [ok("POST", "/depositos", {"nombre": n, "descripcion": f"{n} de la tienda"}, T)["id"] for n in DEPOSITOS]
    # La semilla de la base ya trae algunos tipos de cliente y metodos de pago: solo se crean los que faltan
    for n in TIPOS_CLIENTE:
        if n not in {t["nombre"] for t in contenido(ok("GET", "/tipos-cliente?size=100", token=T))}:
            ok("POST", "/tipos-cliente", {"nombre": n}, T)
    for n in METODOS_PAGO:
        if n not in {m["nombre"] for m in contenido(ok("GET", "/metodos-pago?size=100", token=T))}:
            ok("POST", "/metodos-pago", {"nombre": n}, T)
    tipos = {t["nombre"]: t["id"] for t in contenido(ok("GET", "/tipos-cliente?size=100", token=T))}
    print(f"  {len(cat)} categorias, {len(uni)} unidades, {len(dep)} depositos, {len(tipos)} tipos de cliente, "
          f"{len(contenido(ok('GET', '/metodos-pago?size=100', token=T)))} metodos de pago")

    paso("Productos, presentaciones y precios")
    productos = []   # {id, nombre, precio, costo, perecedero, base}
    for i, (c, nombre, precio, costo, perecedero) in enumerate(PRODUCTOS, start=1):
        p = ok("POST", "/productos", {"nombre": nombre, "descripcion": f"{nombre} ({c})", "categoriaId": cat[c],
                                      "stockMinimo": 1000 if nombre in PRODUCTOS_BAJO_MINIMO else random.choice([10, 15, 20, 30]),
                                      "esPerecedero": perecedero}, T)
        base = ok("POST", f"/productos/{p['id']}/presentaciones", {
            "unidadMedidaId": uni["Unidad"], "codigoBarras": f"7790{p['id']:08d}", "factorConversion": 1,
            "esUnidadBase": True}, T)
        pr = q2(precio)
        ok("POST", f"/presentaciones/{base['id']}/precios", {"precio": pr}, T)
        ok("POST", f"/presentaciones/{base['id']}/precios",
           {"tipoClienteId": tipos["VIP"], "cantidadMinima": 1, "precio": q2(pr * D("0.95"))}, T)
        ok("POST", f"/presentaciones/{base['id']}/precios",
           {"tipoClienteId": tipos["MAYORISTA"], "cantidadMinima": 12, "precio": q2(pr * D("0.90"))}, T)
        if i % 3 == 1:   # el primero de cada categoria tambien se vende por caja de 12
            caja = ok("POST", f"/productos/{p['id']}/presentaciones", {
                "unidadMedidaId": uni["Caja"], "codigoBarras": f"7791{p['id']:08d}", "factorConversion": 12,
                "esUnidadBase": False}, T)
            ok("POST", f"/presentaciones/{caja['id']}/precios", {"precio": q2(pr * 12 * D("0.92"))}, T)
        productos.append({"id": p["id"], "nombre": nombre, "costo": D(costo), "perecedero": perecedero})
    print(f"  {len(productos)} productos con presentacion base, precios general, VIP y mayorista")

    paso("Clientes, proveedores y cajas")
    for i, nombre in enumerate(CLIENTES):
        ok("POST", "/clientes", {"tipoClienteId": tipos[TIPOS_CLIENTE[i % len(TIPOS_CLIENTE)]],
                                 "documento": f"{10000000 + i * 137}", "nombre": nombre,
                                 "telefono": f"7{random.randint(1000000, 9999999)}",
                                 "email": nombre.lower().replace(" ", ".") + "@mail.com"}, T)
    prov = []
    for empresa, contacto in PROVEEDORES:
        prov.append(ok("POST", "/proveedores", {
            "nombreEmpresa": empresa, "nombreContacto": contacto, "telefono": f"2{random.randint(100000, 999999)}",
            "email": empresa.lower().replace(" ", "") + "@proveedor.com"}, T)["id"])
    ok("PATCH", f"/proveedores/{prov[-1]}/desactivar", token=T)
    for i, idx in enumerate(CAJAS_DEPOSITO, start=1):
        ok("POST", "/cajas", {"nombre": f"Caja {i}", "depositoId": dep[idx]}, T)
    print(f"  {len(CLIENTES)} clientes, {len(prov)} proveedores (1 inactivo), {len(CAJAS_DEPOSITO)} cajas")

    paso("Pedidos y recepciones (compras que dejan el stock)")
    activos = prov[:-1]

    def pedido(deposito, proveedor, items, dias_entrega=7):
        return ok("POST", "/pedidos", {
            "proveedorId": proveedor, "depositoDestinoId": deposito, "fechaEntregaEstimada": None,
            "observacion": "Pedido de la semilla de desarrollo",
            "items": [{"productoId": p["id"], "cantidad": q, "costoUnitario": p["costo"]} for p, q in items]}, T)

    def recibir(ped, porcentaje, dias_vence, descuento=False):
        """Recibe 'porcentaje' de lo pendiente de cada linea, creando un lote por linea."""
        lineas = []
        for it in ped["items"]:
            nombre = it["producto"]["nombre"]
            producto = next(p for p in productos if p["id"] == it["producto"]["id"])
            cantidad = q2(D(str(it["cantidadPendiente"])) * D(porcentaje) / 100) if porcentaje < 100 \
                else D(str(it["cantidadPendiente"]))
            if cantidad <= 0:
                continue
            linea = {"lineaId": it["id"], "cantidad": cantidad}
            if producto["perecedero"] or descuento:
                linea["fechaVencimiento"] = str(datetime.date.today() +
                                                datetime.timedelta(days=dias_vence + random.randint(0, 20)))
            if descuento and nombre in PRODUCTOS_CON_DESCUENTO:
                linea["fechaVencimiento"] = str(datetime.date.today() +
                                                datetime.timedelta(days=6))
                linea["porcentajeDescuentoVencimiento"] = 25
            lineas.append(linea)
        return ok("POST", f"/pedidos/{ped['id']}/recepciones",
                  {"numeroGuia": f"G-{random.randint(1000, 9999)}", "observacion": "Recepcion de la semilla",
                   "items": lineas}, T)

    # Un pedido grande por deposito con venta (los 30 productos), recibido en una o varias entregas
    plan = [(0, 300, [(40, 120), (30, 150), (100, 180)]), (1, 120, [(60, 130), (100, 160)]), (2, 100, [(100, 140)]),
            (3, 80, [(100, 150)])]
    recepciones = 0
    for idx, cantidad, entregas in plan:
        ped = ok("POST", f"/pedidos/{pedido(dep[idx], activos[idx % len(activos)], [(p, cantidad) for p in productos])['id']}"
                 "/enviar", token=T)
        ped_id = ped["id"]
        for n, (pct, dias) in enumerate(entregas):
            ped = ok("GET", f"/pedidos/{ped_id}", token=T)
            # el ultimo tramo del deposito central trae los lotes con descuento por vencimiento cercano
            recibir(ped, pct, dias, descuento=(idx == 0 and n == len(entregas) - 1))
            recepciones += 1

    chicos = [(0, "RECIBIDO_PARCIAL", 50), (0, "RECIBIDO_PARCIAL", 40), (1, "RECIBIDO", 100), (0, "ENVIADO", None),
              (2, "ENVIADO", None), (0, "BORRADOR", None), (1, "BORRADOR", None), (2, "CANCELADO", None)]
    for k, (idx, estado, pct) in enumerate(chicos):
        items = [(p, random.choice([20, 30, 50])) for p in random.sample(productos, 5)]
        ped = pedido(dep[idx], activos[(k + 3) % len(activos)], items)
        if estado == "BORRADOR":
            continue
        ped = ok("POST", f"/pedidos/{ped['id']}/enviar", token=T)
        if estado == "CANCELADO":
            ok("POST", f"/pedidos/{ped['id']}/cancelar", token=T)
        elif pct:
            recibir(ped, pct, 100)
            recepciones += 1
    print(f"  {len(plan) + len(chicos)} pedidos (todos los estados) y {recepciones} recepciones")

    paso("Movimientos manuales de inventario")
    stock = contenido(ok("GET", f"/inventario?depositoId={dep[0]}&conStock=true&size=100", token=T))
    lotes = [(s["lote"]["id"], D(str(s["stockActual"]))) for s in stock]
    # entradas manuales a otros depositos (lote nuevo)
    for k in range(3):
        p = productos[k * 5]
        ok("POST", "/inventario/entradas", {
            "depositoId": dep[4 + k % 2], "observacion": "Stock inicial de sucursal",
            "items": [{"productoId": p["id"], "cantidad": 60, "lote": {
                "fechaVencimiento": str(datetime.date.today() + datetime.timedelta(days=200)),
                "costoUnitarioCompra": p["costo"]}}]}, T)
    for k in range(3):
        ok("POST", "/inventario/traslados", {"depositoOrigenId": dep[0], "depositoDestinoId": dep[5 + k % 3],
                                             "observacion": "Reposicion de sucursal",
                                             "items": [{"loteId": lotes[k][0], "cantidad": 25},
                                                       {"loteId": lotes[k + 10][0], "cantidad": 15}]}, T)
    for k, motivo in enumerate(["Merma por rotura", "Consumo interno", "Producto danado"]):
        ok("POST", "/inventario/salidas", {"depositoId": dep[0], "motivo": motivo,
                                           "items": [{"loteId": lotes[20 + k][0], "cantidad": 3}]}, T)
    for k, motivo in enumerate(["Conteo fisico", "Diferencia de inventario", "Correccion de carga"]):
        loteid, actual = lotes[30 + k]
        ok("POST", "/inventario/ajustes", {"depositoId": dep[0], "loteId": loteid,
                                           "stockReal": actual + (2 if k % 2 else -2), "motivo": motivo}, T)
    print("  3 entradas, 3 traslados, 3 salidas y 3 ajustes manuales")

    paso("Reposiciones a costos mas altos")
    compras_con_variacion()
    print("\nFase 1 lista. Siguiente: ejecutar usuarios.sql y luego `seed_dev.py 2`.")


# ---------------------------------------------------------------------------------------------- fase 2

USUARIOS_CAJA = ["cajero1", "cajero2", "cajero3", "cajero4", "supervisor1", "supervisor2", "encargado1"]
MOTIVOS_DEV = ["Producto en mal estado", "Cliente arrepentido", "Producto equivocado", "Envase danado", "Producto vencido"]


def fase2():
    tokens = {u: login(u, PASS_FINAL) for u in USUARIOS_CAJA}
    if not all(tokens.values()):
        raise SystemExit("Faltan usuarios: ejecute usuarios.sql antes de la fase 2 (contrasena Admin#2026)")
    T = tokens["supervisor1"]    # supervisor: anula ventas y consulta
    admin = login("admin", PASS_TEMPORAL) or login("admin", PASS_FINAL)

    paso("Cargando catalogo")
    cajas = sorted(contenido(ok("GET", "/cajas?size=100", token=T)), key=lambda c: c["id"])
    metodos = {m["nombre"]: m["id"] for m in contenido(ok("GET", "/metodos-pago?size=100", token=T))}
    tipos = {t["id"]: t["nombre"] for t in contenido(ok("GET", "/tipos-cliente?size=100", token=T))}
    clientes = contenido(ok("GET", "/clientes?estado=ACTIVO&size=100", token=T))
    precios = {n: q2(p) for _, n, p, _, _ in PRODUCTOS}
    catalogo = []   # (presentacionBaseId, nombre, precio)
    for p in contenido(ok("GET", "/productos?size=100", token=T)):
        base = next(x for x in contenido(ok("GET", f"/productos/{p['id']}/presentaciones", token=T))
                    if x.get("esUnidadBase"))
        catalogo.append((base["id"], p["nombre"], precios[p["nombre"]]))
    print(f"  {len(cajas)} cajas, {len(catalogo)} productos, {len(clientes)} clientes")

    estadisticas = {"ventas": 0, "anuladas": 0, "devoluciones": 0, "cambios": 0, "turnos": 0, "fallos": 0}

    def vender(token, caja_deposito_ok=True):
        lineas = random.sample(catalogo, random.randint(1, 4))
        items = [(pid, nombre, precio, random.randint(1, 5)) for pid, nombre, precio in lineas]
        cliente = random.choice(clientes) if random.random() < 0.4 else None
        factor = D("0.95") if cliente and tipos.get(cliente["tipoCliente"]["id"]) == "VIP" else D(1)
        total = sum(q2(precio * factor) * cant for _, _, precio, cant in items)
        con_descuento = any(nombre in PRODUCTOS_CON_DESCUENTO for _, nombre, _, _ in items)
        forma = "EFECTIVO" if con_descuento else random.choices(
            ["EFECTIVO", "TARJETA", "TRANSFERENCIA", "QR", "MIXTO"], [40, 25, 10, 8, 17])[0]
        if forma == "EFECTIVO":
            pagos = [{"metodoPagoId": metodos["EFECTIVO"], "monto": q2((total / 10).to_integral_value(rounding="ROUND_CEILING") * 10)}]
        elif forma == "MIXTO" and total > 2:
            efectivo = q2(total / 2)
            pagos = [{"metodoPagoId": metodos["EFECTIVO"], "monto": efectivo},
                     {"metodoPagoId": metodos["TARJETA"], "monto": q2(total - efectivo),
                      "referencia": f"AUT-{random.randint(1000, 9999)}"}]
        else:
            metodo = "TARJETA" if forma == "MIXTO" else forma
            pagos = [{"metodoPagoId": metodos[metodo], "monto": q2(total),
                      "referencia": f"{metodo[:3]}-{random.randint(10000, 99999)}"}]
        cuerpo = {"clienteId": cliente["id"] if cliente else None,
                  "items": [{"presentacionId": pid, "cantidad": cant} for pid, _, _, cant in items], "pagos": pagos}
        v = intentar("POST", "/ventas", cuerpo, token, {"Idempotency-Key": str(uuid.uuid4())})
        if v is None:
            estadisticas["fallos"] += 1
        else:
            estadisticas["ventas"] += 1
        return v

    def jornada(usuario, caja, cerrar, n_ventas, con_anulacion, con_devolucion, con_cambio):
        token = tokens[usuario]
        turno = ok("POST", f"/cajas/{caja['id']}/turnos", {"montoApertura": random.choice([100, 150, 200, 300])}, token)
        ventas = [v for v in (vender(token) for _ in range(n_ventas)) if v]
        if len(ventas) < 3:
            raise SystemExit(f"Muy pocas ventas en el turno {turno['id']}: revise el stock o los precios")
        usadas = set()

        if con_anulacion:
            v = ventas[0]
            if intentar("POST", f"/ventas/{v['id']}/anular", {"motivo": "Error de digitacion del cajero"}, T):
                estadisticas["anuladas"] += 1
                usadas.add(v["id"])
        libres = [v for v in ventas if v["id"] not in usadas]

        if con_devolucion and libres:
            v = libres[1] if len(libres) > 1 else libres[0]
            linea = v["lineas"][0]
            d = intentar("POST", f"/ventas/{v['id']}/devoluciones", {
                "motivo": random.choice(MOTIVOS_DEV), "reembolsar": True,
                "metodoPagoId": metodos["TARJETA"],
                "items": [{"ventaDetalleId": linea["id"], "cantidad": 1, "reingresaStock": random.choice([True, True, False])}]},
                token, {"Idempotency-Key": str(uuid.uuid4())})
            if d:
                estadisticas["devoluciones"] += 1
                usadas.add(v["id"])
        libres = [v for v in libres if v["id"] not in usadas]

        if con_cambio:
            # devolver una linea cara y llevar un jabon (el mas barato): el credito alcanza y sobra
            for v in libres:
                linea = next((l for l in v["lineas"] if q2(l["precioUnitario"]) >= 6), None)
                if linea:
                    nuevo = min(catalogo, key=lambda c: c[2])
                    c = intentar("POST", f"/ventas/{v['id']}/cambios", {
                        "motivo": "Cambio por otro producto",
                        "devueltos": [{"ventaDetalleId": linea["id"], "cantidad": 1}],
                        "nuevos": [{"presentacionId": nuevo[0], "cantidad": 1}]},
                        token, {"Idempotency-Key": str(uuid.uuid4())})
                    if c:
                        estadisticas["cambios"] += 1
                    break

        intentar("POST", f"/turnos/{turno['id']}/movimientos",
                 {"tipo": "INGRESO", "monto": 50, "motivo": "Fondo adicional de cambio"}, token)
        intentar("POST", f"/turnos/{turno['id']}/movimientos",
                 {"tipo": "EGRESO", "monto": 20, "motivo": "Pago de flete a proveedor"}, token)

        if cerrar:
            esperado = D(str(ok("GET", f"/turnos/{turno['id']}", token=token)["efectivoEsperado"]))
            declarado = max(D(0), q2(esperado + random.choice([D(0), D(0), D(0), D("-2"), D("-5"), D("3.5"), D("1")])))
            ok("POST", f"/turnos/{turno['id']}/cerrar", {"montoDeclarado": declarado}, token)
            estadisticas["turnos"] += 1
        return turno

    paso("Turnos cerrados (14), con ventas, anulaciones, devoluciones y cambios")
    orden_cajas = [0, 1, 4, 7, 2, 3, 5, 8, 9, 6, 0, 1, 4, 7]    # Caja 1,2,5,8,3,4,...
    for k in range(14):
        usuario = USUARIOS_CAJA[k % len(USUARIOS_CAJA)]
        jornada(usuario, cajas[orden_cajas[k]], cerrar=True, n_ventas=5 + k % 4,
                con_anulacion=(k % 3 == 0), con_devolucion=(k % 6 != 5), con_cambio=(k % 5 == 2))
        print(f"  turno {k + 1:2d}: {usuario:12s} {cajas[orden_cajas[k]]['nombre']}", end="\r")
    print()

    paso("Turnos abiertos (3)")
    for usuario, caja in (("cajero1", cajas[0]), ("cajero2", cajas[1]), ("cajero3", cajas[4])):
        jornada(usuario, caja, cerrar=False, n_ventas=4, con_anulacion=False, con_devolucion=False, con_cambio=False)

    paso("Ventas con precio manual (supervisor)")
    precios_manuales()

    print("\nResumen:", estadisticas)
    print("\nFase 2 lista. Siguiente: ejecutar fechas.sql para repartir las fechas.")


def compras_con_variacion():
    """Reposiciones de los primeros productos a costos mas altos, para ver la evolucion del costo de compra."""
    T = token_admin()
    deposito = next(d["id"] for d in contenido(ok("GET", "/depositos?size=100", token=T)) if d["nombre"] == DEPOSITOS[0])
    proveedores = [p["id"] for p in contenido(ok("GET", "/proveedores?estado=ACTIVO&size=100", token=T))]
    por_nombre = {p["nombre"]: p["id"] for p in contenido(ok("GET", "/productos?size=100", token=T))}
    datos = {n: (D(c), per) for _, n, _, c, per in PRODUCTOS}
    # (cuantos de los primeros productos, factor de aumento, proveedor)
    reposiciones = [(6, D("1.06"), proveedores[0]), (3, D("1.12"), proveedores[1]), (2, D("1.09"), proveedores[0])]
    for cuantos, factor, proveedor in reposiciones:
        nombres = [n for _, n, _, _, _ in PRODUCTOS][:cuantos]
        pedido = ok("POST", "/pedidos", {
            "proveedorId": proveedor, "depositoDestinoId": deposito, "fechaEntregaEstimada": None,
            "observacion": f"Reposicion con aumento de costo ({factor})",
            "items": [{"productoId": por_nombre[n], "cantidad": 40, "costoUnitario": q2(datos[n][0] * factor)}
                      for n in nombres]}, T)
        ok("POST", f"/pedidos/{pedido['id']}/enviar", token=T)
        ped = ok("GET", f"/pedidos/{pedido['id']}", token=T)
        items = []
        for it in ped["items"]:
            linea = {"lineaId": it["id"], "cantidad": it["cantidadPendiente"]}
            if datos[it["producto"]["nombre"]][1]:
                linea["fechaVencimiento"] = str(datetime.date.today() + datetime.timedelta(days=150))
            items.append(linea)
        ok("POST", f"/pedidos/{ped['id']}/recepciones", {"numeroGuia": f"G-{random.randint(1000, 9999)}",
                                                         "observacion": "Reposicion", "items": items}, T)
    print(f"  {len(reposiciones)} reposiciones recibidas con costos mas altos")


def precios_manuales():
    """Un supervisor abre un turno y hace ventas con precio manual (algunas bajo el costo), y lo cierra."""
    token = login("supervisor1", PASS_FINAL)
    if not token:
        raise SystemExit("Falta el usuario supervisor1: ejecute usuarios.sql antes")
    cajas = sorted(contenido(ok("GET", "/cajas?size=100", token=token)), key=lambda c: c["id"])
    precios = {n: (q2(p), q2(c)) for _, n, p, c, _ in PRODUCTOS}
    catalogo = []
    for p in contenido(ok("GET", "/productos?size=100", token=token)):
        base = next(x for x in contenido(ok("GET", f"/productos/{p['id']}/presentaciones", token=token))
                    if x.get("esUnidadBase"))
        catalogo.append((base["id"], p["nombre"]))

    turno = None
    for caja in cajas[2:]:
        turno = intentar("POST", f"/cajas/{caja['id']}/turnos", {"montoApertura": 200}, token)
        if turno:
            break
    if not turno:
        raise SystemExit("No hay una caja libre para el turno del supervisor")

    motivos = ["Producto golpeado", "Promocion de cierre", "Cliente frecuente", "Envase danado", "Liquidacion"]
    hechas = bajo_costo = 0
    for i in range(8):
        pid, nombre = random.choice(catalogo)
        lista, costo = precios[nombre]
        es_bajo_costo = i % 3 == 0
        precio = q2(costo * D("0.80")) if es_bajo_costo else q2(lista * D("0.85"))
        cantidad = random.randint(1, 3)
        venta = intentar("POST", "/ventas", {
            "items": [{"presentacionId": pid, "cantidad": cantidad, "precioUnitario": precio,
                       "motivoPrecio": random.choice(motivos)}],
            "pagos": [{"metodoPagoId": next(m["id"] for m in contenido(ok("GET", "/metodos-pago?size=100", token=token))
                                              if m["nombre"] == "EFECTIVO"), "monto": 500}]},
            token, {"Idempotency-Key": str(uuid.uuid4())})
        if venta:
            hechas += 1
            bajo_costo += 1 if es_bajo_costo else 0
    esperado = D(str(ok("GET", f"/turnos/{turno['id']}", token=token)["efectivoEsperado"]))
    ok("POST", f"/turnos/{turno['id']}/cerrar", {"montoDeclarado": esperado}, token)
    print(f"  turno {turno['id']} de supervisor1: {hechas} ventas con precio manual ({bajo_costo} bajo el costo)")


if __name__ == "__main__":
    if len(sys.argv) != 2 or sys.argv[1] not in ("1", "2", "precios-manuales", "compras-variacion"):
        raise SystemExit(__doc__)
    {"1": fase1, "2": fase2, "precios-manuales": precios_manuales,
     "compras-variacion": compras_con_variacion}[sys.argv[1]]()
