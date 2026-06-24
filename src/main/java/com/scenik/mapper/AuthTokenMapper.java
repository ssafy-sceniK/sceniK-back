package com.scenik.mapper;

import com.scenik.domain.AuthToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthTokenMapper {
    int insert(AuthToken token);
    AuthToken findActiveByHash(@Param("tokenHash") String tokenHash);
    int revokeByHash(@Param("tokenHash") String tokenHash);
    int revokeByUserId(@Param("userId") Long userId);
}
