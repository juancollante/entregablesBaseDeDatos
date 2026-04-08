package com.logistica.logistica_auth.domain.port.out;

import com.logistica.logistica_auth.domain.model.UserAccount;

public interface AccessTokenPort {

    String createAccessToken(UserAccount user);
}
