package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.model.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    DtoUser toDto(User user);
    List<DtoUser> toDtoList(List<User> users);

    User toUser(DtoUser dtoUser);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    User fromDtoUserIU(DtoUserIU dtoUserIU);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(DtoUserIU dto, @MappingTarget User user);
}