package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.infrastructure.adapter.out.persistence.DepositoEntity;
import com.tienda.backend.inventario.application.model.StockDetalle;
import com.tienda.backend.inventario.domain.model.Inventario;
import com.tienda.backend.lote.infrastructure.adapter.out.persistence.LoteEntity;
import com.tienda.backend.producto.infrastructure.adapter.out.persistence.ProductoEntity;
import org.springframework.stereotype.Component;

@Component
public class InventarioPersistenceMapper {

    public Inventario toDomain(InventarioEntity entity) {
        return new Inventario(
                entity.getId(),
                entity.getDeposito().getId(),
                entity.getLote().getId(),
                entity.getStockActual(),
                entity.getVersion());
    }

    public InventarioEntity toEntity(Inventario domain) {
        return InventarioEntity.builder()
                .id(domain.getId())
                .deposito(DepositoEntity.builder().id(domain.getDepositoId()).build())
                .lote(LoteEntity.builder().id(domain.getLoteId()).build())
                .stockActual(domain.getStockActual())
                .version(domain.getVersion())
                .build();
    }

    /** Requiere depósito, lote y producto cargados (entity graph). */
    public StockDetalle toStockDetalle(InventarioEntity entity) {
        DepositoEntity deposito = entity.getDeposito();
        LoteEntity lote = entity.getLote();
        ProductoEntity producto = lote.getProducto();
        return new StockDetalle(
                entity.getId(),
                deposito.getId(), deposito.getNombre(),
                producto.getId(), producto.getNombre(),
                lote.getId(), lote.getNumeroLote(), lote.getFechaVencimiento(),
                entity.getStockActual(),
                null);
    }
}
