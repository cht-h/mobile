package com.example.notscratch;

import java.util.HashMap;
import java.util.Map;

public class VariableManager {
    private final Map<String, Integer> variables = new HashMap<>();

    public void declareVariable(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Имя переменной не может быть пустым");
        }
        if (variables.containsKey(name)) {
            throw new RuntimeException("Переменная '" + name + "' уже объявлена");
        }
        variables.put(name, 0);
    }

    public void setVariable(String name, int value) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Переменная '" + name + "' не была объявлена");
        }
        variables.put(name, value);
    }

    public int getVariable(String name) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Использование необъявленной переменной '" + name + "'");
        }
        return variables.get(name);
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public void clear() {
        variables.clear();
    }

    public Map<String, Integer> getAllVariables() {
        return new HashMap<>(variables);
    }
}