package giuliacrepaldi.BStudent_Management.enums.identity;

// Le richieste chiuse vengono cancellate (minimizzazione dei dati): gli stati finali
// servono solo a rendere esplicito il ciclo di vita e i blocchi durante la transazione
public enum RegistrationRequestStatus {
    pending,
    confirmed,
    rejected,
    expired
}
