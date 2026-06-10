package com.scenik.mapper;

import com.scenik.domain.OAuthAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OAuthAccountMapper {
    OAuthAccount findByProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    int insert(OAuthAccount account);
}
