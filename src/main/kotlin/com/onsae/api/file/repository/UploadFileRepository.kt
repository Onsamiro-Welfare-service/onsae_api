package com.onsae.api.file.repository

import com.onsae.api.file.entity.UploadFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * UploadFile 엔티티를 위한 Repository 인터페이스
 * 
 * JpaRepository를 상속받아서 기본적인 CRUD 기능을 자동으로 제공합니다.
 * - save(): 저장
 * - findById(): ID로 조회
 * - findAll(): 전체 조회
 * - delete(): 삭제
 * 
 * 필요한 경우 커스텀 쿼리 메서드를 추가할 수 있습니다.
 */
@Repository
interface UploadFileRepository : JpaRepository<UploadFile, Long> {
    
    /**
     * 특정 업로드(Upload)에 속한 모든 파일 조회
     * @param uploadId 업로드 ID
     * @return 업로드 파일 목록
     */
    fun findByUploadIdOrderByUploadOrder(uploadId: Long): List<UploadFile>
    
    /**
     * 특정 업로드(Upload)에 속한 파일 개수 조회
     * @param uploadId 업로드 ID
     * @return 파일 개수
     */
    fun countByUploadId(uploadId: Long): Int
}
