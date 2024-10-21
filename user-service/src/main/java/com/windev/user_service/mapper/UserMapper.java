package com.windev.user_service.mapper;

import com.windev.user_service.dto.request.UpdateUserRequest;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
    void updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);
}
