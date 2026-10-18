package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schoolId;

    @NotBlank
    @Column(name = "name", length = 255, nullable = false)
    private String schoolName;

    @Column(name = "street", length = 255)
    private String schoolAddress;

    @Column(name = "street_number", length = 30)
    private String schoolStreetNumber;

    @Column(name = "city", length = 150)
    private String schoolCity;

    @Column(name = "province", length = 100)
    private String schoolProvince;

    @Column(name = "postal_code", length = 20)
    private String schoolPostalCode;
}
