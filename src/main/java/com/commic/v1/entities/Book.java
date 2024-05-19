package com.commic.v1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
@EqualsAndHashCode(of = "id") // Chỉ dùng id cho equals và hashCode
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String author;
    private String description;
    private String status;

    @ManyToMany(mappedBy = "books")
    private Set<Category> categories;

    private String thumbnail;

    @OneToMany(mappedBy = "book")
    private Set<Chapter> chapters;

    @OneToMany(mappedBy = "book")
    private Set<Statistical> statisticals;
}
