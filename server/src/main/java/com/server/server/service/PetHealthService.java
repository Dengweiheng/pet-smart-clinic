package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.PetCreateRequest;
import com.server.server.dto.PetHealthRecordRequest;
import com.server.server.dto.PetHealthRecordResponse;
import com.server.server.dto.PetResponse;
import com.server.server.entity.Pet;
import com.server.server.entity.PetHealthRecord;
import com.server.server.mapper.PetHealthRecordMapper;
import com.server.server.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物基础信息与健康档案管理服务层
 * <p>
 * 提供宠物的建档、资料更新、档案查询，以及过敏史、慢性病、疫苗与用药历史的维护。
 */
@Service
@RequiredArgsConstructor
public class PetHealthService {

    private final PetMapper petMapper;
    private final PetHealthRecordMapper petHealthRecordMapper;

    /**
     * 新增宠物档案
     *
     * @param request 包含宠物品种、性别、体重、出生日期及主人信息的请求对象
     * @return 宠物信息响应 DTO
     */
    public PetResponse createPet(PetCreateRequest request) {
        Pet pet = new Pet();
        pet.setOwnerUserId(Long.valueOf(request.getOwnerUserId()));
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setWeightKg(request.getWeightKg());
        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            pet.setBirthDate(LocalDate.parse(request.getBirthDate()));
        }
        petMapper.insert(pet);

        PetResponse response = new PetResponse();
        response.setPetId(String.valueOf(pet.getId()));
        response.setOwnerUserId(String.valueOf(pet.getOwnerUserId()));
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setGender(pet.getGender());
        response.setBirthDate(pet.getBirthDate() == null ? null : pet.getBirthDate().toString());
        response.setWeightKg(pet.getWeightKg());
        return response;
    }

    /**
     * 更新已有宠物的基本信息
     *
     * @param petId 宠物 ID
     * @param request 待更新的宠物信息
     * @return 更新后的宠物信息响应 DTO
     */
    public PetResponse updatePet(String petId, PetCreateRequest request) {
        Pet pet = petMapper.selectById(Long.valueOf(petId));
        if (pet == null) return null;

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setWeightKg(request.getWeightKg());
        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            pet.setBirthDate(LocalDate.parse(request.getBirthDate()));
        }
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.updateById(pet);

        PetResponse response = new PetResponse();
        response.setPetId(String.valueOf(pet.getId()));
        response.setOwnerUserId(String.valueOf(pet.getOwnerUserId()));
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setGender(pet.getGender());
        response.setBirthDate(pet.getBirthDate() == null ? null : pet.getBirthDate().toString());
        response.setWeightKg(pet.getWeightKg());
        return response;
    }

    /**
     * 根据宠物 ID 获取宠物基础档案详情
     *
     * @param petId 宠物 ID
     * @return 宠物信息响应 DTO
     */
    public PetResponse getPet(String petId) {
        Pet pet = petMapper.selectById(Long.valueOf(petId));
        if (pet == null) {
            return null;
        }
        PetResponse response = new PetResponse();
        response.setPetId(String.valueOf(pet.getId()));
        response.setOwnerUserId(String.valueOf(pet.getOwnerUserId()));
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setGender(pet.getGender());
        response.setBirthDate(pet.getBirthDate() == null ? null : pet.getBirthDate().toString());
        response.setWeightKg(pet.getWeightKg());
        return response;
    }

    /**
     * 保存或更新宠物的健康医疗档案（过敏史、既往病史、疫苗接种、用药史等）
     *
     * @param petId 宠物 ID
     * @param request 健康档案内容
     * @return 更新后的健康档案响应 DTO
     */
    public PetHealthRecordResponse saveOrUpdateHealthRecord(String petId, PetHealthRecordRequest request) {
        Long pid = Long.valueOf(petId);
        Pet pet = petMapper.selectById(pid);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }

        LambdaQueryWrapper<PetHealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetHealthRecord::getPetId, pid);
        PetHealthRecord record = petHealthRecordMapper.selectOne(wrapper);
        if (record == null) {
            record = new PetHealthRecord();
            record.setPetId(pid);
            record.setAllergies(request.getAllergies());
            record.setChronicDiseases(request.getChronicDiseases());
            record.setVaccineNotes(request.getVaccineNotes());
            record.setMedicationNotes(request.getMedicationNotes());
            record.setUpdatedBy(pet.getOwnerUserId());
            petHealthRecordMapper.insert(record);
        } else {
            record.setAllergies(request.getAllergies());
            record.setChronicDiseases(request.getChronicDiseases());
            record.setVaccineNotes(request.getVaccineNotes());
            record.setMedicationNotes(request.getMedicationNotes());
            record.setUpdatedBy(pet.getOwnerUserId());
            petHealthRecordMapper.updateById(record);
        }

        return toHealthResponse(record);
    }

    /**
     * 查询指定宠物的健康医疗档案
     *
     * @param petId 宠物 ID
     * @return 宠物健康档案
     */
    public PetHealthRecordResponse getHealthRecord(String petId) {
        LambdaQueryWrapper<PetHealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetHealthRecord::getPetId, Long.valueOf(petId));
        PetHealthRecord record = petHealthRecordMapper.selectOne(wrapper);
        return record == null ? null : toHealthResponse(record);
    }

    /**
     * 实体对象向健康响应 DTO 转换
     */
    private PetHealthRecordResponse toHealthResponse(PetHealthRecord record) {
        PetHealthRecordResponse response = new PetHealthRecordResponse();
        response.setPetId(String.valueOf(record.getPetId()));
        response.setAllergies(record.getAllergies());
        response.setChronicDiseases(record.getChronicDiseases());
        response.setVaccineNotes(record.getVaccineNotes());
        response.setMedicationNotes(record.getMedicationNotes());
        return response;
    }
}

