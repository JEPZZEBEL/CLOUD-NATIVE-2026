package cl.duoc.tickets.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class RolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Azure AD puede entregar:
        // 1) "roles": ["Admin", "User"] (App Roles)
        // 2) "scp": "Tickets.Read Tickets.Write" (Scopes)
        // 3) "extension_Roles": "ADMIN,USER" (claim custom, como en algunas guías del profe)

        // 1) App Roles (array)
        Object rolesClaim = jwt.getClaims().get("roles");
        if (rolesClaim instanceof Collection<?> rolesList && !rolesList.isEmpty()) {
            return rolesList.stream()
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 2) Custom claim (string separado por coma)
        String extensionRoles = jwt.getClaimAsString("extension_Roles");
        if (extensionRoles != null && !extensionRoles.isBlank()) {
            return Arrays.stream(extensionRoles.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 3) Scopes (opcionales) -> los exponemos como SCOPE_x
        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            return Arrays.stream(scp.split("\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(scope -> "SCOPE_" + scope)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
