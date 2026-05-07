package com.logistica.logistica_seguimiento.domain.port.in;

import com.logistica.logistica_seguimiento.domain.model.SedeOption;

import java.util.List;

public interface ListSedesUseCase {

    List<SedeOption> listActivas();
}
