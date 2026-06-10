package com.scenik.mapper;

import com.scenik.domain.OAuthLoginCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OAuthLoginCodeMapper {
    int insert(OAuthLoginCode code);
    OAuthLoginCode findActiveByHashForUpdate(@Param("codeHash") String codeHash);
    int consume(@Param("oauthLoginCodeId") Long oauthLoginCodeId);
}
