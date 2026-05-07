package com.logistica.logistica_seguimiento.application.service;

import com.logistica.logistica_seguimiento.adapter.out.persistence.entity.SedeEntity;
import com.logistica.logistica_seguimiento.adapter.out.persistence.repository.SedeJpaRepository;
import com.logistica.logistica_seguimiento.domain.model.SedeOption;
import com.logistica.logistica_seguimiento.domain.port.in.ListSedesUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SedeCatalogService implements ListSedesUseCase {

    private final SedeJpaRepository sedeRepo;

    public SedeCatalogService(SedeJpaRepository sedeRepo) {
        this.sedeRepo = sedeRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SedeOption> listActivas() {
        return sedeRepo.findAllActivasWithMunicipioOrdenadas().stream().map(SedeCatalogService::toOption).toList();
    }

    private static SedeOption toOption(SedeEntity s) {
        var m = s.getMunicipio();
        return new SedeOption(
                s.getCodigo(),
                s.getNombre(),
                m.getNombre(),
                m.getCodigoDane()
        );
    }
}
