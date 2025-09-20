package com.onsae.api.file.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "upload_files")
class UploadFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    var upload: Upload = Upload()

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    var fileType: FileType = FileType.IMAGE

    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String = ""

    @Column(name = "original_name", length = 255)
    var originalName: String? = null

    @Column(name = "file_path", nullable = false, length = 500)
    var filePath: String = ""

    @Column(name = "file_size")
    var fileSize: Long? = null

    @Column(name = "mime_type", length = 100)
    var mimeType: String? = null

    @Column(name = "duration_seconds")
    var durationSeconds: Int? = null

    @Column(name = "image_width")
    var imageWidth: Int? = null

    @Column(name = "image_height")
    var imageHeight: Int? = null

    @Column(name = "thumbnail_path", length = 500)
    var thumbnailPath: String? = null

    @Column(name = "upload_order", nullable = false)
    var uploadOrder: Int = 1

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}