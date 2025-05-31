package com.example.notscratch;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.notscratch.R;
import com.example.notscratch.Block;
import com.example.notscratch.BlockType;

public class BlockEditorDialog {
    public interface OnBlockSavedListener {
        void onBlockSaved(Block block);
    }

    public static void show(Context context, Block block, boolean isEditMode, OnBlockSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(isEditMode ? "Редактировать блок" : "Создать блок");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_block_editor, null);
        builder.setView(view);

        EditText etCode = view.findViewById(R.id.etCode);
        EditText etDescription = view.findViewById(R.id.etDescription);
        TextView tvType = view.findViewById(R.id.tvType);

        etCode.setText(block.getCode());
        etDescription.setText(block.getDescription());
        tvType.setText(block.getType().getDisplayName());

        setupInputForType(block.getType(), etCode);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(context, "Код блока не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                validateBlockCode(block.getType(), code);
                block.setCode(code);
                block.setDescription(etDescription.getText().toString());
                listener.onBlockSaved(block);
            } catch (Exception e) {
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Отмена", null);

        if (block.getType() == BlockType.IF_STATEMENT) {
            builder.setNeutralButton("Добавить Else", (dialog, which) -> {
                String code = etCode.getText().toString();
                if (!code.contains("else")) {
                    code = code.replaceFirst("\\}", " else \\{\\}");
                    etCode.setText(code);
                }
            });
        }

        builder.show();
    }

    private static void setupInputForType(BlockType type, EditText etCode) {
        switch (type) {
            case VARIABLE_DECLARATION:
                etCode.setHint("int x, y, z");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                break;
            case ASSIGNMENT:
                etCode.setHint("x = 5");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                break;
            case ARITHMETIC:
                etCode.setHint("a + b * c");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                break;
            case IF_STATEMENT:
                etCode.setHint("if (x > 5) { y = 10 }");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case WHILE_LOOP:
                etCode.setHint("while (x < 10) { x = x + 1 }");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case FOR_LOOP:
                etCode.setHint("for (i = 0; i < 10; i = i + 1) { print(i) }");
                etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
        }
    }

    private static void validateBlockCode(BlockType type, String code) {
        switch (type) {
            case VARIABLE_DECLARATION:
                if (!code.startsWith("int ")) {
                    throw new RuntimeException("Объявление переменной должно начинаться с 'int '");
                }
                if (code.substring(4).trim().isEmpty()) {
                    throw new RuntimeException("Не указаны имена переменных");
                }
                break;
            case ASSIGNMENT:
                if (!code.contains("=")) {
                    throw new RuntimeException("Присваивание должно содержать знак '='");
                }
                if (code.indexOf("=") == 0 || code.indexOf("=") == code.length() - 1) {
                    throw new RuntimeException("Некорректное присваивание");
                }
                break;
            case IF_STATEMENT:
                if (!code.contains("if (") || !code.contains(")")) {
                    throw new RuntimeException("Условие должно быть в скобках: if (условие)");
                }
                if (!code.contains("{")) {
                    throw new RuntimeException("Отсутствует открывающая фигурная скобка");
                }
                break;
            case WHILE_LOOP:
                if (!code.contains("while (") || !code.contains(")")) {
                    throw new RuntimeException("Условие должно быть в скобках: while (условие)");
                }
                if (!code.contains("{")) {
                    throw new RuntimeException("Отсутствует открывающая фигурная скобка");
                }
                break;
            case FOR_LOOP:
                if (!code.contains("for (") || !code.contains(")")) {
                    throw new RuntimeException("Цикл for должен содержать скобки: for (инициализация; условие; инкремент)");
                }
                if (!code.contains("{")) {
                    throw new RuntimeException("Отсутствует открывающая фигурная скобка");
                }
                break;
        }
    }
}