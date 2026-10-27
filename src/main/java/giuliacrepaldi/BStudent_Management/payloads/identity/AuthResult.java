package giuliacrepaldi.BStudent_Management.payloads.identity;

// Uso interno service -> controller: il token va nel cookie httpOnly, al client si restituisce solo user
public record AuthResult(String accessToken, LoggedUserDTO user) {

    // Il token non deve finire nei log
    @Override
    public String toString() {
        return "AuthResult[userId=" + (user == null ? null : user.userId()) + "]";
    }
}
