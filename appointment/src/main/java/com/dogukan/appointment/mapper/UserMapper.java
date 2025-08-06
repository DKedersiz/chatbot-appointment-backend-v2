package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.DtoUser;
import com.dogukan.appointment.model.User;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface UserMapper {
    DtoUser toDto(User user);
    User toUser(DtoUser dtoUser);
    List<DtoUser> toDtoList(List<User> userList);
}
