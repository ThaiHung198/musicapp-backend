package com.musicapp.backend.service;

import com.musicapp.backend.dto.PageInfo;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.tag.CreateTagRequest;
import com.musicapp.backend.dto.tag.TagAdminViewDto;
import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.entity.Tag;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.TagMapper;
import com.musicapp.backend.repository.SongRepository;
import com.musicapp.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final SongRepository songRepository;
    private final TagMapper tagMapper;

    public List<TagDto> getAllTags() {
        return tagRepository.findAllByOrderByNameAsc()
                .stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }

    public PagedResponse<TagAdminViewDto> getAllTagsForAdmin(Pageable pageable) {
        Page<Object[]> pageResult = tagRepository.findAllWithSongCount(pageable);
        List<TagAdminViewDto> dtos = pageResult.getContent().stream()
                .map(result -> new TagAdminViewDto(
                        ((Number) result[0]).longValue(),
                        (String) result[1],
                        ((Number) result[2]).longValue()
                ))
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(pageResult);

        return new PagedResponse<>(dtos, pageInfo);
    }

    @Transactional
    public TagDto createTag(CreateTagRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Tag đã tồn tại với tên: " + request.getName());
        }
        Tag tag = new Tag();
        tag.setName(request.getName());
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    @Transactional
    public TagDto updateTag(Long id, CreateTagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tag với ID: " + id));

        if (!tag.getName().equalsIgnoreCase(request.getName()) && tagRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Tag đã tồn tại với tên: " + request.getName());
        }

        tag.setName(request.getName());
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toDto(updatedTag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tag với ID: " + id));

        long songCount = songRepository.countByTagsContains(tag);
        if (songCount > 0) {
            throw new BadRequestException("Không thể xóa tag '" + tag.getName() + "' vì nó đang được sử dụng bởi " + songCount + " bài hát.");
        }

        tagRepository.delete(tag);
    }
}