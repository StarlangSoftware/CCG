package CCG;

import java.util.*;

public class CCGWord {

    private final LinkedList<String> ccg;
    private final LinkedList<Type> types;
    private String universalDependency;

    public CCGWord(String word, String universalDependency) {
        this.ccg = new LinkedList<>();
        this.types = new LinkedList<>();
        this.universalDependency = universalDependency;
        splitCCG(word);
        if (ccg.size() == 1) {
            String ccg = this.ccg.getFirst();
            if (ccg.charAt(0) == '(') {
                splitCCG(ccg.substring(1, ccg.length() - 1));
            }
        }
    }

    public CCGWord(LinkedList<String> ccg, LinkedList<Type> types, String universalDependency) {
        this.ccg = ccg;
        this.types = types;
        this.universalDependency = universalDependency;
    }

    public String getUniversalDependency() {
        return universalDependency;
    }

    protected void splitCCG(String word) {
        this.ccg.clear();
        this.types.clear();
        int open = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '(') {
                open++;
                current.append(word.charAt(i));
            } else if (word.charAt(i) == ')') {
                open--;
                current.append(word.charAt(i));
            } else {
                if (word.charAt(i) == '/') {
                    if (open == 0) {
                        ccg.add(current.toString());
                        types.add(Type.FORWARD);
                        current = new StringBuilder();
                    } else {
                        current.append(word.charAt(i));
                    }
                } else if (word.charAt(i) == '\\') {
                    if (open == 0) {
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

    public boolean composition(CCGWord next) {
        boolean isCrossed = !this.types.getLast().equals(next.types.getFirst());
        ccg.removeLast();
        types.removeLast();
        this.universalDependency = next.universalDependency;
        next.removeFirstCCG();
        add(next);
        return isCrossed;
    }

    // forward and backward application
    public void application(String universalDependency) {
        ccg.removeLast();
        types.removeLast();
        this.universalDependency = universalDependency;
        if (ccg.size() == 1) {
            String ccg = getCCG();
            if (ccg.charAt(0) == '(') {
                splitCCG(ccg.substring(1, ccg.length() - 1));
            }
        }
    }

    public int size() {
        return ccg.size();
    }

    public String toString() {
        if (ccg.size() == 1) {
            return ccg.getFirst();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ccg.size(); i++) {
            if (i != 0) {
                if (types.get(i - 1).equals(Type.FORWARD)) {
                    sb.append("/");
                } else {
                    sb.append("\\");
                }
            }
            sb.append(ccg.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public CCGWord clone() {
        return new CCGWord((LinkedList<String>) this.ccg.clone(), (LinkedList<Type>) this.types.clone(), this.universalDependency);
    }
    @Override
    public int hashCode() {
        return Objects.hash(ccg);
    }
}
