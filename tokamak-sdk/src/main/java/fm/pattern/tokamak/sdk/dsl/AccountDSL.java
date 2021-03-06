package fm.pattern.tokamak.sdk.dsl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fm.pattern.tokamak.sdk.AccountsClient;
import fm.pattern.tokamak.sdk.JwtClientProperties;
import fm.pattern.tokamak.sdk.RolesClient;
import fm.pattern.tokamak.sdk.commons.Result;
import fm.pattern.tokamak.sdk.model.AccountRepresentation;
import fm.pattern.tokamak.sdk.model.RoleRepresentation;

public class AccountDSL extends AbstractDSL<AccountDSL, AccountRepresentation> {

    private AccountsClient client = new AccountsClient(JwtClientProperties.getEndpoint());
	private RolesClient rolesClient = new RolesClient(JwtClientProperties.getEndpoint());
	
    private String id = null;
    private String username = "usr_" + randomAlphanumeric(15);
    private String password = randomAlphanumeric(8) + "aA4$";

    private Set<RoleRepresentation> roles = new HashSet<RoleRepresentation>();
    private Set<String> rolesLookup = new HashSet<String>();

    public static AccountDSL account() {
        return new AccountDSL();
    }

    public AccountDSL withId(String id) {
        this.id = id;
        return this;
    }

    public AccountDSL withUsername(String username) {
        this.username = username;
        return this;
    }

    public AccountDSL withPassword(String password) {
        this.password = password;
        return this;
    }

    public AccountDSL withRoles(RoleRepresentation... roles) {
        this.roles.addAll(Arrays.asList(roles));
        return this;
    }

	public AccountDSL withRoles(String... roles) {
		this.rolesLookup.clear();
		this.rolesLookup.addAll(Arrays.asList(roles));
		return this;
	}
    
    public AccountRepresentation build() {
        AccountRepresentation representation = create();
        if (!shouldPersist()) {
            return representation;
        }

        Result<AccountRepresentation> response = client.create(representation, super.getToken().getAccessToken());
        if (response.rejected()) {
            throw new IllegalStateException("Unable to create account, response from server: " + response.getErrors().toString());
        }

        return response.getInstance();
    }

    private AccountRepresentation create() {
    	resolve();
    	
        AccountRepresentation representation = new AccountRepresentation();
        if (StringUtils.isNotBlank(id)) {
            representation.setId(id);
        }
        representation.setUsername(username);
        representation.setPassword(password);
        representation.setRoles(roles);
        return representation;
    }

	private void resolve() {
		if (super.getToken() == null) {
			return;
		}

		for (String name : rolesLookup) {
			Result<RoleRepresentation> result = rolesClient.findByName(name, super.getToken().getAccessToken());
			if (result.accepted()) {
				roles.add(result.getInstance());
			}
		}

	}
    
}
