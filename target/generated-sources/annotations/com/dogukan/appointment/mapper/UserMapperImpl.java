package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.DtoUser;
import com.dogukan.appointment.model.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-07T11:20:28+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.2 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public DtoUser toDto(User user) {
        if ( user == null ) {
            return null;
        }

        DtoUser dtoUser = new DtoUser();

        return dtoUser;
    }

    @Override
    public User toUser(DtoUser dtoUser) {
        if ( dtoUser == null ) {
            return null;
        }

        User user = new User();

        return user;
    }

    @Override
    public List<DtoUser> toDtoList(List<User> userList) {
        if ( userList == null ) {
            return null;
        }

        List<DtoUser> list = new ArrayList<DtoUser>( userList.size() );
        for ( User user : userList ) {
            list.add( toDto( user ) );
        }

        return list;
    }
}
