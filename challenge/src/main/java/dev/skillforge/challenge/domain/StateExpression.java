package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.domain.model.SimulationState;

/**
 * Évaluateur d'expressions d'état minimales : {@code <variable> <op> <entier>} avec les opérateurs
 * {@code >= <= == != > <}. Déterministe et pur. {@code null}, vide ou {@code "default"} = vrai
 * (utilisé comme transition par défaut) ; une expression non analysable = faux.
 */
public final class StateExpression {

    private static final String[] OPERATORS = {">=", "<=", "==", "!=", ">", "<"};

    private StateExpression() {
    }

    public static boolean matches(String expression, SimulationState state) {
        if (expression == null) {
            return true;
        }
        String expr = expression.trim();
        if (expr.isEmpty() || expr.equalsIgnoreCase("default")) {
            return true;
        }
        for (String op : OPERATORS) {
            int at = expr.indexOf(op);
            if (at > 0) {
                String variable = expr.substring(0, at).trim();
                int rhs;
                try {
                    rhs = Integer.parseInt(expr.substring(at + op.length()).trim());
                } catch (NumberFormatException e) {
                    return false;
                }
                return compare(state.get(variable), op, rhs);
            }
        }
        return false;
    }

    /** Vraie condition (variable comparée à un seuil), à l'exclusion du repli {@code default}/vide. */
    public static boolean isConditional(String expression) {
        if (expression == null) {
            return false;
        }
        String expr = expression.trim();
        return !expr.isEmpty() && !expr.equalsIgnoreCase("default");
    }

    private static boolean compare(int lhs, String op, int rhs) {
        return switch (op) {
            case ">=" -> lhs >= rhs;
            case "<=" -> lhs <= rhs;
            case "==" -> lhs == rhs;
            case "!=" -> lhs != rhs;
            case ">" -> lhs > rhs;
            case "<" -> lhs < rhs;
            default -> false;
        };
    }
}
