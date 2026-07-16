package ru.ast.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String title;

//    @Lob
    @Column(columnDefinition = "TEXT", name = "full_text")
    private String fullText;

    @Column(length = 15)
    private String status = "UPLOADED";

    @Column(name = "uploaded_path", length = 200)
    private String uploadPath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Book(String title, String fullText, String uploadPath) {
        this.title = title;
        this.fullText = fullText;
        this.uploadPath = uploadPath;
    }
}
