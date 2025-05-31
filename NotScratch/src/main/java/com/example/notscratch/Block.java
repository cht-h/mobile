package com.example.notscratch;

public class Block {
    private final BlockType type;
    private String code;
    private String description;

    public Block(BlockType type, String code, String description) {
        this.type = type;
        this.code = code;
        this.description = description;
    }

    public BlockType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

enum BlockType {
    VARIABLE_DECLARATION("Объявление переменной", "#4CAF50"),
    ASSIGNMENT("Присваивание", "#2196F3"),
    ARITHMETIC("Арифметика", "#FF9800"),
    IF_STATEMENT("Условие", "#9C27B0"),
    WHILE_LOOP("Цикл While", "#607D8B"),
    FOR_LOOP("Цикл For", "#795548");

    private final String displayName;
    private final String color;

    BlockType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}