package com.logistica.logistica_auth.adapter.out.persistence;

import com.logistica.logistica_auth.adapter.out.persistence.entity.UsuarioEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.UsuarioJpaRepository;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserAuthenticationAdapter implements UserAuthenticationPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public UserAuthenticationAdapter(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Optional<UserAccount> findActiveByEmail(String email) {
        return usuarioJpaRepository.findByEmailIgnoreCaseAndActivoIsTrue(email).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findActiveById(UUID id) {
        return usuarioJpaRepository.findById(id)
                .filter(UsuarioEntity::isActivo)
                .map(this::toDomain);
    }

    private UserAccount toDomain(UsuarioEntity e) {
        return new UserAccount(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getNombre(),
                e.getApellido(),
                e.getRol().getCodigo(),
                e.getCodigoSedeAsignada(),
                e.isActivo()
        );
    }
}
