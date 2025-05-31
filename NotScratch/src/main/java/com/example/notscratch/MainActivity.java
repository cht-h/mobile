package com.example.notscratch;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notscratch.BlocksAdapter;
import com.example.notscratch.Block;
import com.example.notscratch.BlockType;
import com.example.notscratch.VariableManager;
import com.example.notscratch.BlockDragHelper;
import com.example.notscratch.ExpressionEvaluator;
import com.example.notscratch.BlockEditorDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BlocksAdapter.OnBlockInteractionListener {
    private RecyclerView blocksRecyclerView;
    private BlocksAdapter blocksAdapter;
    private final List<Block> blocksList = new ArrayList<>();
    private final VariableManager variableManager = new VariableManager();
    private ExpressionEvaluator expressionEvaluator;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expressionEvaluator = new ExpressionEvaluator(variableManager);
        initializeViews();
        setupRecyclerView();
        setupButtons();
    }

    private void initializeViews() {
        blocksRecyclerView = findViewById(R.id.blocksRecyclerView);
        tvOutput = findViewById(R.id.tvOutput);
    }

    private void setupRecyclerView() {
        blocksAdapter = new BlocksAdapter(blocksList, this);
        blocksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        blocksRecyclerView.setAdapter(blocksAdapter);

        ItemTouchHelper.Callback callback = new BlockDragHelper(blocksAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(blocksRecyclerView);
    }

    private void setupButtons() {
        findViewById(R.id.btnAddBlock).setOnClickListener(v -> showAddBlockDialog());
        findViewById(R.id.btnClearAll).setOnClickListener(v -> clearAllBlocks());
        findViewById(R.id.btnRun).setOnClickListener(v -> executeProgram());
    }

    private void showAddBlockDialog() {
        String[] blockTypes = {
                "Объявление переменных (int x, y)",
                "Присваивание (x = 5)",
                "Арифметика (a + b * c)",
                "Условие (if x > 5)",
                "Цикл while (while x < 10)",
                "Цикл for (for i = 0; i < 5; i = i + 1)"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Добавить блок")
                .setItems(blockTypes, (dialog, which) -> {
                    BlockType type = BlockType.values()[which];
                    String template = getTemplateForType(type);
                    Block newBlock = new Block(type, template, "");
                    showEditBlockDialog(newBlock, false);
                })
                .show();
    }

    private String getTemplateForType(BlockType type) {
        switch (type) {
            case VARIABLE_DECLARATION: return "int ";
            case ASSIGNMENT: return " = ";
            case ARITHMETIC: return "";
            case IF_STATEMENT: return "if () {\n  \n}";
            case WHILE_LOOP: return "while () {\n  \n}";
            case FOR_LOOP: return "for (; ; ) {\n  \n}";
            default: return "";
        }
    }

    private void showEditBlockDialog(Block block, boolean isEditMode) {
        BlockEditorDialog.show(this, block, isEditMode, editedBlock -> {
            if (!isEditMode) {
                blocksList.add(editedBlock);
                blocksAdapter.notifyItemInserted(blocksList.size() - 1);
                Snackbar.make(blocksRecyclerView, "Блок добавлен", Snackbar.LENGTH_SHORT).show();
            } else {
                blocksAdapter.notifyItemChanged(blocksList.indexOf(editedBlock));
            }
        });
    }

    private void clearAllBlocks() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Очистить всё")
                .setMessage("Вы уверены, что хотите удалить все блоки?")
                .setPositiveButton("Да", (dialog, which) -> {
                    blocksList.clear();
                    blocksAdapter.notifyDataSetChanged();
                    Snackbar.make(blocksRecyclerView, "Все блоки удалены", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void executeProgram() {
        variableManager.clear();
        StringBuilder output = new StringBuilder();
        output.append("=== Начало выполнения программы ===\n\n");

        try {
            for (int i = 0; i < blocksList.size(); i++) {
                Block block = blocksList.get(i);
                try {
                    output.append("▶ Блок #").append(i + 1).append(" (").append(block.getType().getDisplayName()).append("):\n");

                    switch (block.getType()) {
                        case VARIABLE_DECLARATION:
                            handleVariableDeclaration(block.getCode());
                            output.append("  Объявлены переменные: ").append(block.getCode().replace("int", "").trim()).append("\n");
                            break;
                        case ASSIGNMENT:
                            handleAssignment(block.getCode());
                            output.append("  Присвоено: ").append(block.getCode()).append("\n");
                            break;
                        case ARITHMETIC:
                            int result = expressionEvaluator.evaluate(block.getCode());
                            output.append("  Вычислено: ").append(block.getCode()).append(" = ").append(result).append("\n");
                            break;
                        case IF_STATEMENT:
                            handleIfStatement(block.getCode(), output);
                            break;
                        case WHILE_LOOP:
                            handleWhileLoop(block.getCode(), output);
                            break;
                        case FOR_LOOP:
                            handleForLoop(block.getCode(), output);
                            break;
                    }

                    output.append("\n");
                } catch (Exception e) {
                    String errorMsg = String.format("Ошибка в блоке #%d (%s): %s",
                            i + 1, block.getType().getDisplayName(), e.getMessage());
                    showError(errorMsg);
                    output.append("  ❌ ").append(errorMsg).append("\n\n");
                }
            }

            output.append("=== Состояние переменных ===\n");
            variableManager.getAllVariables().forEach((name, value) ->
                    output.append(name).append(" = ").append(value).append("\n"));

            tvOutput.setText(output.toString());
        } catch (Exception e) {
            showError("Критическая ошибка выполнения: " + e.getMessage());
            tvOutput.setText("❌ Программа завершена с ошибкой\n\n" + e.getMessage());
        }
    }

    private void handleVariableDeclaration(String code) {
        String[] parts = code.replace("int", "").split(",");
        for (String part : parts) {
            String varName = part.trim();
            if (!varName.isEmpty()) {
                variableManager.declareVariable(varName);
            }
        }
    }

    private void handleAssignment(String code) {
        String[] parts = code.split("=", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Некорректное присваивание: " + code);
        }

        String varName = parts[0].trim();
        String expression = parts[1].trim();
        int value = expressionEvaluator.evaluate(expression);
        variableManager.setVariable(varName, value);
    }

    private void handleIfStatement(String code, StringBuilder output) {
        String condition = code.substring(code.indexOf("(") + 1, code.indexOf(")")).trim();
        String trueBlock = extractBlockContent(code, "{", "}");
        String falseBlock = code.contains("else") ?
                extractBlockContent(code.substring(code.indexOf("else")), "{", "}") : null;

        boolean conditionMet = evaluateCondition(condition);
        output.append("  Условие: ").append(condition).append(" - ").append(conditionMet ? "истина" : "ложь").append("\n");

        if (conditionMet) {
            output.append("  Выполняется блок if:\n");
            executeBlock(trueBlock, output);
        } else if (falseBlock != null) {
            output.append("  Выполняется блок else:\n");
            executeBlock(falseBlock, output);
        }
    }

    private void handleWhileLoop(String code, StringBuilder output) {
        String condition = code.substring(code.indexOf("(") + 1, code.indexOf(")")).trim();
        String block = extractBlockContent(code, "{", "}");

        int iteration = 0;
        while (evaluateCondition(condition)) {
            iteration++;
            output.append("  Итерация ").append(iteration).append(": условие ").append(condition).append(" - истина\n");
            executeBlock(block, output);

            if (iteration > 1000) {
                throw new RuntimeException("Превышено максимальное количество итераций (1000). Возможно бесконечный цикл");
            }
        }
        output.append("  Цикл while завершен после ").append(iteration).append(" итераций\n");
    }

    private void handleForLoop(String code, StringBuilder output) {
        String[] parts = code.substring(code.indexOf("(") + 1, code.indexOf(")")).split(";");
        if (parts.length != 3) {
            throw new RuntimeException("Некорректный цикл for. Ожидается формат: for(инициализация; условие; инкремент)");
        }

        String init = parts[0].trim();
        String condition = parts[1].trim();
        String increment = parts[2].trim();
        String block = extractBlockContent(code, "{", "}");

        if (!init.isEmpty()) {
            output.append("  Инициализация: ").append(init).append("\n");
            handleAssignment(init);
        }

        int iteration = 0;
        while (condition.isEmpty() || evaluateCondition(condition)) {
            iteration++;
            if (!condition.isEmpty()) {
                output.append("  Итерация ").append(iteration).append(": условие ").append(condition).append(" - истина\n");
            } else {
                output.append("  Итерация ").append(iteration).append(" (безусловный цикл)\n");
            }

            executeBlock(block, output);

            if (!increment.isEmpty()) {
                handleAssignment(increment);
            }

            if (iteration > 1000) {
                throw new RuntimeException("Превышено максимальное количество итераций (1000). Возможно бесконечный цикл");
            }
        }
        output.append("  Цикл for завершен после ").append(iteration).append(" итераций\n");
    }

    private boolean evaluateCondition(String condition) {
        if (condition.isEmpty()) return true;

        String[] operators = { ">=", "<=", "!=", "==", ">", "<" };
        String op = "";
        int opIndex = -1;

        for (String operator : operators) {
            opIndex = condition.indexOf(operator);
            if (opIndex != -1) {
                op = operator;
                break;
            }
        }

        if (op.isEmpty()) {
            return expressionEvaluator.evaluate(condition) != 0;
        }

        String leftExpr = condition.substring(0, opIndex).trim();
        String rightExpr = condition.substring(opIndex + op.length()).trim();

        int leftVal = expressionEvaluator.evaluate(leftExpr);
        int rightVal = expressionEvaluator.evaluate(rightExpr);

        switch (op) {
            case ">": return leftVal > rightVal;
            case "<": return leftVal < rightVal;
            case ">=": return leftVal >= rightVal;
            case "<=": return leftVal <= rightVal;
            case "==": return leftVal == rightVal;
            case "!=": return leftVal != rightVal;
            default: throw new RuntimeException("Неизвестный оператор: " + op);
        }
    }

    private String extractBlockContent(String code, String startDelim, String endDelim) {
        int start = code.indexOf(startDelim) + 1;
        int end = code.lastIndexOf(endDelim);
        if (start < 0 || end < 0 || start >= end) {
            return "";
        }
        return code.substring(start, end).trim();
    }

    private void executeBlock(String blockCode, StringBuilder output) {
        if (blockCode.isEmpty()) return;

        String[] statements = blockCode.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) continue;

            try {
                if (trimmed.contains("=")) {
                    handleAssignment(trimmed);
                    output.append("    Выполнено: ").append(trimmed).append("\n");
                } else {
                    int result = expressionEvaluator.evaluate(trimmed);
                    output.append("    Вычислено: ").append(trimmed).append(" = ").append(result).append("\n");
                }
            } catch (Exception e) {
                output.append("    ❌ Ошибка: ").append(trimmed).append(" - ").append(e.getMessage()).append("\n");
                throw e;
            }
        }
    }

    private void showError(String message) {
        Snackbar.make(blocksRecyclerView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error))
                .setTextColor(getResources().getColor(R.color.white))
                .show();
    }

    @Override
    public void onBlockMoved(int from, int to) {
    }

    @Override
    public void onBlockEdited(int position) {
        if (position >= 0 && position < blocksList.size()) {
            showEditBlockDialog(blocksList.get(position), true);
        }
    }

    @Override
    public void onBlockDeleted(int position) {
        if (position >= 0 && position < blocksList.size()) {
            Block deletedBlock = blocksList.get(position);
            blocksList.remove(position);
            blocksAdapter.notifyItemRemoved(position);

            Snackbar.make(blocksRecyclerView, "Блок удалён", Snackbar.LENGTH_LONG)
                    .setAction("Отменить", v -> {
                        blocksList.add(position, deletedBlock);
                        blocksAdapter.notifyItemInserted(position);
                    })
                    .show();
        }
    }
}