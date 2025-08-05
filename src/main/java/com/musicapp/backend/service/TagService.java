package com.musicapp.backend.service;

import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.entity.Tag;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.TagMapper;
import com.musicapp.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    
    public List<TagDto> getAllTags() {
        return tagRepository.findAllOrderByNameAsc()
                .stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public List<TagDto> searchTags(String keyword) {
        return tagRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword)
                .stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public TagDto getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return tagMapper.toDto(tag);
    }
    
    @Transactional
    public TagDto createTag(String name) {
        // Check if tag already exists
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceAlreadyExistsException("Tag already exists with name: " + name);
        }
        
        Tag tag = Tag.builder()
                .name(name)
                .build();
        
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }
    
    @Transactional
    public TagDto updateTag(Long id, String name) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        
        // Check if new name conflicts with existing tag
        if (!tag.getName().equalsIgnoreCase(name) && tagRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceAlreadyExistsException("Tag already exists with name: " + name);
        }
        
        tag.setName(name);
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toDto(updatedTag);
    }
    
    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with id: " + id);
        }
        tagRepository.deleteById(id);
    }
}
