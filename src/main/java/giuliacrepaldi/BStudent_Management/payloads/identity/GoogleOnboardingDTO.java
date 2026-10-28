package giuliacrepaldi.BStudent_Management.payloads.identity;

// Precompila il form di onboarding: il cookie firmato è httpOnly e il frontend non può leggerlo
public record GoogleOnboardingDTO(String email, String firstName, String lastName) {
}
