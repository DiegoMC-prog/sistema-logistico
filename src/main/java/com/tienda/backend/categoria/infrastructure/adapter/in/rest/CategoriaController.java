package com.tienda.backend.categoria.infrastructure.adapter.in.rest;

import com.tienda.backend.categoria.application.port.in.CrearCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.EditarCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.EliminarCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.ListarCategoriaUseCase;
import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.categoria.infrastructure.adapter.in.rest.dto.CategoriaRequest;
import com.tienda.backend.categoria.infrastructure.adapter.in.rest.dto.CategoriaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CrearCategoriaUseCase crearCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final ListarCategoriaUseCase listarCategoriaUseCase;
    private final EliminarCategoriaUseCase eliminarCategoriaUseCase;

    @PostMapping
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request){
        Categoria categoria = new Categoria(request.nombre());
        Categoria guardada =  crearCategoriaUseCase.create(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoriaResponse(guardada.getId(), guardada.getNombre()));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategorias(){
        List<CategoriaResponse> categorias = listarCategoriaUseCase.listar().stream()
                .map(cat -> new CategoriaResponse(cat.getId(), cat.getNombre()))
                .toList();

        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> editarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request){
        Categoria categoria = editarCategoriaUseCase.editar(id, new Categoria(request.nombre()));
        return ResponseEntity.ok(new CategoriaResponse(categoria.getId(), categoria.getNombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id){
        eliminarCategoriaUseCase.eliminar(id);

        return  ResponseEntity.noContent().build();
    }
}
