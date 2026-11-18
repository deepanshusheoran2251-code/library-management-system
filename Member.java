package library.model;

public class Member extends Person {

    private String memberType;   // "Student" or "Faculty"
    private String uid;
    private String department;

    // ── Full constructor ─────────────────────────────────────────────────────
    public Member(int id, String name, String email, String phone,
                  String memberType, String uid, String department) {
        super(id, name, email, phone);
        this.memberType  = memberType;
        this.uid         = uid;
        this.department  = department;
    }

    // ── New member constructor (no id yet) ───────────────────────────────────
    public Member(String name, String email, String phone,
                  String memberType, String uid, String department) {
        this(0, name, email, phone, memberType, uid, department);
    }

    // ── Person ───────────────────────────────────────────────────────────────
    @Override
    public String getRole() { return memberType; }

    /** Loan period: 30 days for Faculty, 14 days for Student */
    public int getLoanPeriodDays() {
        return "Faculty".equalsIgnoreCase(memberType) ? 30 : 14;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getMemberType()  { return memberType; }
    public String getUid()         { return uid; }
    public String getDepartment()  { return department; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setMemberType(String t)  { this.memberType  = t; }
    public void setUid(String u)         { this.uid         = u; }
    public void setDepartment(String d)  { this.department  = d; }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " (" + memberType + " – " + uid + ")";
    }
}
