package com.windev.comment_service.mapper;

import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.dto.response.ReactionDto;
import com.windev.comment_service.dto.response.UserDto;
import com.windev.comment_service.entity.Comment;
import com.windev.comment_service.payload.response.CommentWithUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    // Ánh xạ các trường cơ bản từ Comment sang CommentDto
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    CommentDto toDto(Comment comment);

    Comment toEntity(CreateCommentRequest request);

    // Phương thức này để xử lý danh sách childComments đệ quy
    default List<CommentDto> mapChildComments(List<Comment> childComments) {
        return childComments == null
                ? null
                : childComments
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Ánh xạ từ Comment và UserDto sang CommentWithUserResponse
    @Mapping(source = "comment.id", target = "id")  // Lấy "id" từ Comment
    @Mapping(source = "comment.articleId", target = "articleId")
    @Mapping(source = "comment.content", target = "content")
    @Mapping(source = "comment.createdAt", target = "createdAt")
    @Mapping(source = "comment.updatedAt", target = "updatedAt")
    @Mapping(source = "userDto.id", target = "userId")  // Lấy "userId" từ UserDto
    @Mapping(source = "userDto.username", target = "username")  // Lấy "username" từ UserDto
    @Mapping(source = "comment.parentComment.id", target = "parentCommentId")
    CommentWithUserResponse toDtoWithUser(Comment comment, UserDto userDto);

    default List<CommentWithUserResponse> mapChildCommentsWithUser(List<Comment> childComments, UserDto userDto) {
        if (childComments == null) {
            return null;
        }
        return childComments.stream()
                .map(comment -> toDtoWithUser(comment, userDto))
                .collect(Collectors.toList());
    }
    // Phương thức ánh xạ từ parentComment sang parentCommentId
    default Long mapParentComment(Comment parentComment) {
        return parentComment != null ? parentComment.getId() : null;
    }

    // Phương thức chuyển đổi bình luận cùng với các bình luận con
    default CommentDto toDtoWithChildren(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto dto = toDto(comment);
        dto.setChildComments(mapChildComments(comment.getChildComments())); // Đệ quy để ánh xạ bình luận con
        return dto;
    }

    // Phương thức để thêm danh sách reactions vào CommentWithUserResponse
    default CommentWithUserResponse toDtoWithReactions(Comment comment, UserDto userDto, List<ReactionDto> reactions) {
        CommentWithUserResponse response = toDtoWithUser(comment, userDto);
        response.setReactions(reactions);  // Thêm danh sách reactions vào
        return response;
    }
}
