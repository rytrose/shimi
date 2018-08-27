package mr1.NLP;

class Stemmer {
    private static final int INC = 50;
    /* renamed from: b */
    private char[] f1b = new char[INC];
    /* renamed from: i */
    private int f2i = 0;
    private int i_end = 0;
    /* renamed from: j */
    private int f3j;
    /* renamed from: k */
    private int f4k;

    public void add(char ch) {
        if (this.f2i == this.f1b.length) {
            char[] new_b = new char[(this.f2i + INC)];
            for (int c = 0; c < this.f2i; c++) {
                new_b[c] = this.f1b[c];
            }
            this.f1b = new_b;
        }
        char[] cArr = this.f1b;
        int i = this.f2i;
        this.f2i = i + 1;
        cArr[i] = ch;
    }

    public void add(char[] w, int wLen) {
        int c;
        if (this.f2i + wLen >= this.f1b.length) {
            char[] new_b = new char[((this.f2i + wLen) + INC)];
            for (c = 0; c < this.f2i; c++) {
                new_b[c] = this.f1b[c];
            }
            this.f1b = new_b;
        }
        for (c = 0; c < wLen; c++) {
            char[] cArr = this.f1b;
            int i = this.f2i;
            this.f2i = i + 1;
            cArr[i] = w[c];
        }
    }

    public String toString() {
        return new String(this.f1b, 0, this.i_end);
    }

    public int getResultLength() {
        return this.i_end;
    }

    public char[] getResultBuffer() {
        return this.f1b;
    }

    private final boolean cons(int i) {
        switch (this.f1b[i]) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            case 'y':
                if (i == 0 || !cons(i - 1)) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    /* renamed from: m */
    private final int m1m() {
        int n = 0;
        int i = 0;
        while (i <= this.f3j) {
            if (cons(i)) {
                i++;
            } else {
                i++;
                while (i <= this.f3j) {
                    if (cons(i)) {
                        i++;
                        n++;
                        while (i <= this.f3j) {
                            if (cons(i)) {
                                i++;
                            } else {
                                i++;
                            }
                        }
                        return n;
                    }
                    i++;
                }
                return n;
            }
        }
        return 0;
    }

    private final boolean vowelinstem() {
        for (int i = 0; i <= this.f3j; i++) {
            if (!cons(i)) {
                return true;
            }
        }
        return false;
    }

    private final boolean doublec(int j) {
        if (j >= 1 && this.f1b[j] == this.f1b[j - 1]) {
            return cons(j);
        }
        return false;
    }

    private final boolean cvc(int i) {
        if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2)) {
            return false;
        }
        int ch = this.f1b[i];
        if (ch == 119 || ch == 120 || ch == 121) {
            return false;
        }
        return true;
    }

    private final boolean ends(String s) {
        int l = s.length();
        int o = (this.f4k - l) + 1;
        if (o < 0) {
            return false;
        }
        for (int i = 0; i < l; i++) {
            if (this.f1b[o + i] != s.charAt(i)) {
                return false;
            }
        }
        this.f3j = this.f4k - l;
        return true;
    }

    private final void setto(String s) {
        int l = s.length();
        int o = this.f3j + 1;
        for (int i = 0; i < l; i++) {
            this.f1b[o + i] = s.charAt(i);
        }
        this.f4k = this.f3j + l;
    }

    /* renamed from: r */
    private final void m2r(String s) {
        if (m1m() > 0) {
            setto(s);
        }
    }

    private final void step1() {
        if (this.f1b[this.f4k] == 's') {
            if (ends("sses")) {
                this.f4k -= 2;
            } else if (ends("ies")) {
                setto("i");
            } else if (this.f1b[this.f4k - 1] != 's') {
                this.f4k--;
            }
        }
        if (ends("eed")) {
            if (m1m() > 0) {
                this.f4k--;
            }
        } else if ((ends("ed") || ends("ing")) && vowelinstem()) {
            this.f4k = this.f3j;
            if (ends("at")) {
                setto("ate");
            } else if (ends("bl")) {
                setto("ble");
            } else if (ends("iz")) {
                setto("ize");
            } else if (doublec(this.f4k)) {
                this.f4k--;
                int ch = this.f1b[this.f4k];
                if (ch == 108 || ch == 115 || ch == 122) {
                    this.f4k++;
                }
            } else if (m1m() == 1 && cvc(this.f4k)) {
                setto("e");
            }
        }
    }

    private final void step2() {
        if (ends("y") && vowelinstem()) {
            this.f1b[this.f4k] = 'i';
        }
    }

    private final void step3() {
        if (this.f4k != 0) {
            switch (this.f1b[this.f4k - 1]) {
                case 'a':
                    if (ends("ational")) {
                        m2r("ate");
                        return;
                    } else if (ends("tional")) {
                        m2r("tion");
                        return;
                    } else {
                        return;
                    }
                case 'c':
                    if (ends("enci")) {
                        m2r("ence");
                        return;
                    } else if (ends("anci")) {
                        m2r("ance");
                        return;
                    } else {
                        return;
                    }
                case 'e':
                    if (ends("izer")) {
                        m2r("ize");
                        return;
                    }
                    return;
                case 'g':
                    if (ends("logi")) {
                        m2r("log");
                        return;
                    }
                    return;
                case 'l':
                    if (ends("bli")) {
                        m2r("ble");
                        return;
                    } else if (ends("alli")) {
                        m2r("al");
                        return;
                    } else if (ends("entli")) {
                        m2r("ent");
                        return;
                    } else if (ends("eli")) {
                        m2r("e");
                        return;
                    } else if (ends("ousli")) {
                        m2r("ous");
                        return;
                    } else {
                        return;
                    }
                case 'o':
                    if (ends("ization")) {
                        m2r("ize");
                        return;
                    } else if (ends("ation")) {
                        m2r("ate");
                        return;
                    } else if (ends("ator")) {
                        m2r("ate");
                        return;
                    } else {
                        return;
                    }
                case 's':
                    if (ends("alism")) {
                        m2r("al");
                        return;
                    } else if (ends("iveness")) {
                        m2r("ive");
                        return;
                    } else if (ends("fulness")) {
                        m2r("ful");
                        return;
                    } else if (ends("ousness")) {
                        m2r("ous");
                        return;
                    } else {
                        return;
                    }
                case 't':
                    if (ends("aliti")) {
                        m2r("al");
                        return;
                    } else if (ends("iviti")) {
                        m2r("ive");
                        return;
                    } else if (ends("biliti")) {
                        m2r("ble");
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private final void step4() {
        switch (this.f1b[this.f4k]) {
            case 'e':
                if (ends("icate")) {
                    m2r("ic");
                    return;
                } else if (ends("ative")) {
                    m2r("");
                    return;
                } else if (ends("alize")) {
                    m2r("al");
                    return;
                } else {
                    return;
                }
            case 'i':
                if (ends("iciti")) {
                    m2r("ic");
                    return;
                }
                return;
            case 'l':
                if (ends("ical")) {
                    m2r("ic");
                    return;
                } else if (ends("ful")) {
                    m2r("");
                    return;
                } else {
                    return;
                }
            case 's':
                if (ends("ness")) {
                    m2r("");
                    return;
                }
                return;
            default:
                return;
        }
    }

    private final void step5() {
        if (this.f4k != 0) {
            switch (this.f1b[this.f4k - 1]) {
                case 'a':
                    if (!ends("al")) {
                        return;
                    }
                    break;
                case 'c':
                    if (!ends("ance")) {
                        if (ends("ence")) {
                            break;
                        }
                        return;
                    }
                    break;
                case 'e':
                    if (ends("er")) {
                        break;
                    }
                    return;
                case 'i':
                    if (ends("ic")) {
                        break;
                    }
                    return;
                case 'l':
                    if (!ends("able")) {
                        if (ends("ible")) {
                            break;
                        }
                        return;
                    }
                    break;
                case 'n':
                    if (!(ends("ant") || ends("ement") || ends("ment"))) {
                        if (ends("ent")) {
                            break;
                        }
                        return;
                    }
                case 'o':
                    if (!(ends("ion") && this.f3j >= 0 && (this.f1b[this.f3j] == 's' || this.f1b[this.f3j] == 't'))) {
                        if (ends("ou")) {
                            break;
                        }
                        return;
                    }
                case 's':
                    if (ends("ism")) {
                        break;
                    }
                    return;
                case 't':
                    if (!ends("ate")) {
                        if (ends("iti")) {
                            break;
                        }
                        return;
                    }
                    break;
                case 'u':
                    if (ends("ous")) {
                        break;
                    }
                    return;
                case 'v':
                    if (ends("ive")) {
                        break;
                    }
                    return;
                case 'z':
                    if (ends("ize")) {
                        break;
                    }
                    return;
                default:
                    return;
            }
            if (m1m() > 1) {
                this.f4k = this.f3j;
            }
        }
    }

    private final void step6() {
        this.f3j = this.f4k;
        if (this.f1b[this.f4k] == 'e') {
            int a = m1m();
            if (a > 1 || (a == 1 && !cvc(this.f4k - 1))) {
                this.f4k--;
            }
        }
        if (this.f1b[this.f4k] == 'l' && doublec(this.f4k) && m1m() > 1) {
            this.f4k--;
        }
    }

    public void stem() {
        this.f4k = this.f2i - 1;
        if (this.f4k > 1) {
            step1();
            step2();
            step3();
            step4();
            step5();
            step6();
        }
        this.i_end = this.f4k + 1;
        this.f2i = 0;
    }

    public String stem(String word) {
        this.f1b = new char[INC];
        this.f2i = 0;
        this.i_end = 0;
        word = word.toLowerCase();
        add(word.toCharArray(), word.length());
        stem();
        return toString();
    }
}
