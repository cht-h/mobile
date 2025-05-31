package com.example.notscratch;

import com.example.notscratch.VariableManager;
import java.util.Stack;

public class ExpressionEvaluator {
    private final VariableManager variableManager;

    public ExpressionEvaluator(VariableManager variableManager) {
        this.variableManager = variableManager;
    }

    public int evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new RuntimeException("Пустое выражение");
        }

        try {
            String postfix = infixToPostfix(expression);
            return evaluatePostfix(postfix);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка в выражении '" + expression + "': " + e.getMessage());
        }
    }

    private String infixToPostfix(String expression) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        boolean unary = true;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (Character.isLetterOrDigit(c)) {
                while (i < expression.length() &&
                        (Character.isLetterOrDigit(expression.charAt(i)) ||
                                expression.charAt(i) == '_')) {
                    output.append(expression.charAt(i++));
                }
                i--;
                output.append(' ');
                unary = false;
            } else if (c == '(') {
                stack.push(c);
                unary = true;
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop()).append(' ');
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Непарная закрывающая скобка");
                }
                stack.pop();
                unary = false;
            } else {
                if (unary && c == '-') {
                    stack.push('~');
                } else {
                    while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                        output.append(stack.pop()).append(' ');
                    }
                    stack.push(c);
                }
                unary = true;
            }
        }

        while (!stack.isEmpty()) {
            if (stack.peek() == '(') {
                throw new RuntimeException("Непарная открывающая скобка");
            }
            output.append(stack.pop()).append(' ');
        }

        return output.toString().trim();
    }

    private int evaluatePostfix(String postfix) {
        Stack<Integer> stack = new Stack<>();
        String[] tokens = postfix.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (Character.isDigit(token.charAt(0)))
            {
                stack.push(Integer.parseInt(token));
            }
            else if (token.charAt(0) == '-' && token.length() > 1 && Character.isDigit(token.charAt(1)))
            {
                stack.push(Integer.parseInt(token));
            } else if (Character.isLetter(token.charAt(0))) {
                stack.push(variableManager.getVariable(token));
            } else {
                char op = token.charAt(0);
                if (op == '~') {
                    if (stack.isEmpty()) throw new RuntimeException("Недостаточно операндов для унарного оператора");
                    stack.push(-stack.pop());
                } else {
                    if (stack.size() < 2) throw new RuntimeException("Недостаточно операндов для оператора " + op);
                    int b = stack.pop();
                    int a = stack.pop();
                    switch (op) {
                        case '+': stack.push(a + b); break;
                        case '-': stack.push(a - b); break;
                        case '*': stack.push(a * b); break;
                        case '/':
                            if (b == 0) throw new RuntimeException("Деление на ноль");
                            stack.push(a / b);
                            break;
                        case '%':
                            if (b == 0) throw new RuntimeException("Деление на ноль при взятии остатка");
                            stack.push(a % b);
                            break;
                        default: throw new RuntimeException("Неизвестный оператор: " + op);
                    }
                }
            }
        }

        if (stack.size() != 1) {
            throw new RuntimeException("Некорректное выражение");
        }

        return stack.pop();
    }

    private int precedence(char op) {
        switch (op) {
            case '~': return 4;
            case '*':
            case '/':
            case '%': return 3;
            case '+':
            case '-': return 2;
            default: return 0;
        }
    }
}