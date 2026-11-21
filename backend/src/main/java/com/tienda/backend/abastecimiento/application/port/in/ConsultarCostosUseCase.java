package com.tienda.backend.abastecimiento.application.port.in;

import com.tienda.backend.abastecimiento.application.model.CostoFiltro;
import com.tienda.backend.abastecimiento.application.model.HistoricoCosto;
import com.tienda.backend.abastecimiento.application.model.VariacionCostoResumen;
import com.tienda.backend.abastecimiento.application.model.VariacionProveedor;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.List;

/** Histórico de costos por proveedor y su variación (RF-35). */
public interface ConsultarCostosUseCase {

    /** Un registro por par proveedor-producto, primero las mayores alzas a 90 días. */
    PageResult<VariacionCostoResumen> variaciones(CostoFiltro filtro, int page, int size);

    /** Compras confirmadas de un producto (de un proveedor o de todos), de la más reciente a la más antigua. */
    List<HistoricoCosto> historial(Long productoId, Long proveedorId);

    /** Indicador de variación de precios por proveedor, primero los que más subieron. */
    List<VariacionProveedor> resumenProveedores();
}
