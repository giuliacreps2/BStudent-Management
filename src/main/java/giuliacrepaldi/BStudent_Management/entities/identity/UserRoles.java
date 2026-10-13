package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class UserRoles {

    @ManyToMany()
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role_id", columnList = "role_id")
            }
    )
    private List<UserRoles> userRoles;
}
