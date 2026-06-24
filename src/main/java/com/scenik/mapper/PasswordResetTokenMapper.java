package com.scenik.mapper;

import com.scenik.domain.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PasswordResetTokenMapper {
    int insert(PasswordResetToken token);
    PasswordResetToken findActiveByHashForUpdate(@Param("tokenHash") String tokenHash);
    int consume(@Param("passwordResetTokenId") Long passwordResetTokenId);
    int invalidateActiveByUserId(@Param("userId") Long userId);
}
