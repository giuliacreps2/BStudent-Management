package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString


@Entity
public class Schools {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long schoolId;

    @Column(name = "name", length = 250)
    private String schoolName;
    @Column(name = "street", length = 250)
    private String schoolAddress;
    @Column(name = "streetNumber", length = 30)
    private String schoolStreetNumber;
    @Column(name = "city", length = 150)
    private String schoolCity;
    @Column(name = "province", length = 100)
    private String schoolProvince;
    @Column(name = "postal_code", length = 20)
    private String schoolPostalCode;

}
