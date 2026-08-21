package ru.ast.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30, name = "name")
    private String name;

    @Column(name = "description",length = 100)
    private String shortDescription;

    @Column(name = "prompt_style")
    private String promptStyle;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(name = "enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}
