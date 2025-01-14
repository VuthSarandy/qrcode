package com.example.demo.model;


import jdk.jfr.Description;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookModel {

    private Integer id;

    private String title;

    @Description("Should be less than 20 words")
    private String description;

    private String genre;

    private LocalDate dateOfPublication;

    private String author;
}
