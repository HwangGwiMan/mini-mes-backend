package com.github.gwiman.mini_mes_backend.inventory.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryRequest;
import com.github.gwiman.mini_mes_backend.inventory.domain.Inventory;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> findAll() {
        return inventoryRepository.findAll().stream()
            .map(InventoryResponse::from)
            .toList();
    }

    public InventoryResponse findById(Long id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("리소스를 찾을 수 없습니다: " + id));
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        Inventory entity = new Inventory(
            
        );
        return InventoryResponse.from(inventoryRepository.save(entity));
    }

    @Transactional
    public InventoryResponse update(Long id, InventoryRequest request) {
        Inventory entity = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("리소스를 찾을 수 없습니다: " + id));
        
        entity.update(request);
        return InventoryResponse.from(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("리소스를 찾을 수 없습니다: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}