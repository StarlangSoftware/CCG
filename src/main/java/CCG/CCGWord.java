package CCG;

import java.util.LinkedList;

public class CCGWord {

    private final LinkedList<String> ccg;
    private final LinkedList<Type> types;

    public CCGWord(String word) {
        this.ccg = new LinkedList<>();
        this.types = new LinkedList<>();
        boolean open = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '(') {
                open = true;
            } else if (word.charAt(i) == ')') {
                open = false;
            } else {
                if (word.charAt(i) == '/') {
                    if (!open) {
                        ccg.add(current.toString());
                        types.add(Type.FORWARD);
                        current = new StringBuilder();
                    } else {
                        current.append(word.charAt(i));
                    }
                } else if (word.charAt(i) == '\\') {
                    if (!open) {
                        ccg.add(current.toString());
                        types.add(Type.BACKWARD);
                        current = new StringBuilder();
                    } else {
                        current.append(word.charAt(i));
                    }
                } else {
                    current.append(word.charAt(i));
                }
            }
        }
        if (current.length() > 0) {
            ccg.add(current.toString());
        }
    }

    public String getCCG() {
        return ccg.getLast();
    }

    public Type getType() {
        return types.getLast();
    }

    public String getFirstCCG() {
        return ccg.getFirst();
    }

    private void removeFirstCCG() {
        ccg.removeFirst();
    }

    private void add(CCGWord next) {
        this.ccg.addAll(next.ccg);
        this.types.addAll(next.types);
    }

    public void composition(CCGWord next) {
        this.application();
        next.removeFirstCCG();
        add(next);
    }

    // forward and backward application
    public void application() {
        ccg.removeLast();
        types.removeLast();
    }

    public int size() {
        return ccg.size();
    }
}
