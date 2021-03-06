package fm.pattern.jwt.spec.endpoints;

import static fm.pattern.jwt.spec.PatternAssertions.assertThat;
import static fm.pattern.tokamak.sdk.commons.CriteriaRepresentation.criteria;
import static fm.pattern.tokamak.sdk.dsl.AccessTokenDSL.token;
import static fm.pattern.tokamak.sdk.dsl.AccountDSL.account;
import static fm.pattern.tokamak.sdk.dsl.AudienceDSL.audience;
import static fm.pattern.tokamak.sdk.dsl.AuthorityDSL.authority;
import static fm.pattern.tokamak.sdk.dsl.ClientDSL.client;
import static fm.pattern.tokamak.sdk.dsl.ScopeDSL.scope;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import fm.pattern.jwt.spec.AcceptanceTest;
import fm.pattern.tokamak.sdk.ClientCredentials;
import fm.pattern.tokamak.sdk.ClientsClient;
import fm.pattern.tokamak.sdk.JwtClientProperties;
import fm.pattern.tokamak.sdk.TokensClient;
import fm.pattern.tokamak.sdk.commons.PaginatedListRepresentation;
import fm.pattern.tokamak.sdk.commons.Result;
import fm.pattern.tokamak.sdk.model.AccessTokenRepresentation;
import fm.pattern.tokamak.sdk.model.AccountRepresentation;
import fm.pattern.tokamak.sdk.model.AudienceRepresentation;
import fm.pattern.tokamak.sdk.model.AuthorityRepresentation;
import fm.pattern.tokamak.sdk.model.ClientRepresentation;
import fm.pattern.tokamak.sdk.model.ScopeRepresentation;
import fm.pattern.tokamak.sdk.model.SecretsRepresentation;

public class ClientsEndpointAcceptanceTest extends AcceptanceTest {

	private TokensClient tokensClient = new TokensClient(JwtClientProperties.getEndpoint());
	private ClientsClient clientsClient = new ClientsClient(JwtClientProperties.getEndpoint());

	private AccessTokenRepresentation token;

	@Before
	public void before() {
		this.token = token().withClient(TEST_CLIENT_CREDENTIALS).thatIs().persistent().build();
	}

	@Test
	public void shouldBeAbleToCreateAClient() {
		AuthorityRepresentation authority1 = authority().thatIs().persistent(token).build();
		AuthorityRepresentation authority2 = authority().thatIs().persistent(token).build();

		AudienceRepresentation audience1 = audience().thatIs().persistent(token).build();
		AudienceRepresentation audience2 = audience().thatIs().persistent(token).build();

		ScopeRepresentation scope1 = scope().thatIs().persistent(token).build();
		ScopeRepresentation scope2 = scope().thatIs().persistent(token).build();

		ClientRepresentation representation = client().withToken(token).withName("name").withAudiences(audience1, audience2).withScopes(scope1, scope2).withAuthorities(authority1, authority2).withGrantTypes("password", "refresh_token").build();

		Result<ClientRepresentation> response = clientsClient.create(representation, this.token.getAccessToken());
		assertThat(response).accepted().withResponseCode(201);

		ClientRepresentation created = response.getInstance();
		assertThat(created.getId()).isNotNull();
		assertThat(created.getId()).startsWith("cli_");
		assertThat(created.getCreated()).isNotNull();
		assertThat(created.getUpdated()).isNotNull();
		assertThat(created.getCreated()).isEqualTo(created.getUpdated());
		assertThat(created.getClientId()).isEqualTo(representation.getClientId());
		assertThat(created.getClientSecret()).isNull();
		assertThat(created.getName()).isEqualTo("name");

		assertThat(created.getAccessTokenValiditySeconds()).isEqualTo(600);
		assertThat(created.getRefreshTokenValiditySeconds()).isEqualTo(6000);

		assertThat(created.getScopes()).hasSize(2);
		assertThat(created.getScopes()).extracting("name").contains(scope1.getName(), scope2.getName());

		assertThat(created.getGrantTypes()).hasSize(2);
		assertThat(created.getGrantTypes()).extracting("name").contains("password", "refresh_token");

		assertThat(created.getAuthorities()).hasSize(2);
		assertThat(created.getAuthorities()).extracting("name").contains(authority1.getName(), authority2.getName());

		assertThat(created.getAudiences()).hasSize(2);
		assertThat(created.getAudiences()).extracting("name").contains(audience1.getName(), audience2.getName());
	}

	@Test
	public void shouldNotBeAbleToCreateAnInvalidClient() {
		ClientRepresentation representation = client().withToken(token).build();

		Result<ClientRepresentation> response = clientsClient.create(representation, this.token.getAccessToken());
		assertThat(response).rejected().withResponseCode(422).withMessage("A client requires at least one grant type.");
	}

	@Test
	public void shouldNotBeAbleToCreateAClientWithAClientIdThatIsAlreadyInUse() {
		ClientRepresentation representation = client().withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();
		representation.setClientSecret("client_secret");

		Result<ClientRepresentation> response = clientsClient.create(representation, this.token.getAccessToken());
		assertThat(response).rejected().withResponseCode(409).withMessage("This client id is already in use.");
	}

	@Test
	public void shouldBeAbleToUpdateAClient() {
		AuthorityRepresentation authority1 = authority().thatIs().persistent(token).build();
		AuthorityRepresentation authority2 = authority().thatIs().persistent(token).build();

		ClientRepresentation client = client().withAuthorities(authority1).withScopes("accounts:read", "accounts:create").withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();
		pause(1000);

		client.getAuthorities().add(authority2);
		client.getScopes().clear();
		client.setAccessTokenValiditySeconds(100);
		client.setRefreshTokenValiditySeconds(100);
		client.setName("update");
		client.setDescription("updated");

		Result<ClientRepresentation> result = clientsClient.update(client, token.getAccessToken());

		ClientRepresentation updated = result.getInstance();
		assertThat(updated.getId()).startsWith("cli_");
		assertThat(updated.getCreated()).isNotNull();
		assertThat(updated.getUpdated()).isNotNull();
		assertThat(updated.getCreated()).isEqualTo(client.getCreated());
		assertThat(updated.getCreated()).isBefore(updated.getUpdated());
		assertThat(updated.getUpdated()).isAfter(client.getUpdated());
		assertThat(updated.getAccessTokenValiditySeconds()).isEqualTo(100);
		assertThat(updated.getRefreshTokenValiditySeconds()).isEqualTo(100);
		assertThat(updated.getAuthorities()).hasSize(2);
		assertThat(updated.getScopes()).isEmpty();
		assertThat(updated.getName()).isEqualTo("update");
		assertThat(updated.getDescription()).isEqualTo("updated");

	}

	@Test
	public void shouldNotBeAbleToUpdateAnInvalidClient() {
		ClientRepresentation client = client().withScopes("accounts:read", "accounts:create").withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();

		client.getScopes().clear();
		client.getGrantTypes().clear();

		Result<ClientRepresentation> result = clientsClient.update(client, token.getAccessToken());
		assertThat(result).rejected().withResponseCode(422);
	}

	@Test
	public void anAdministratorShouldBeAbleToUpdateAClientSecret() {
		String accountPassword = "alsdifjls3424AA!";
		String newClientSecret = "sldifsladfjsdl#224234AB";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:admin").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation(newClientSecret), t)).accepted().withResponseCode(200);
		assertThat(tokensClient.getAccessToken(new ClientCredentials(client.getClientId(), newClientSecret))).accepted().withResponseCode(200);
	}

	@Test
	public void anAdministratorShouldNotBeAbleToUpdateAClientSecretIfTheClientSecretIsInvalid() {
		String accountPassword = "alsdifjls3424AA!";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:admin").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation("2aaaaA$"), t)).rejected().withError(422, "PWD-0004", "The password must be at least 12 characters.");
	}

	@Test
	public void aUserShouldBeAbleToUpdateAClientSecret() {
		String accountPassword = "alsdifjls3424AA!";
		String currentClientSecret = "sadfljasdf8978wrsls;ASFSADF$$";
		String newClientSecret = "a4423SAFLSDFJSssdflsidfj5234$$";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:user").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withClientSecret(currentClientSecret).withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation(newClientSecret, currentClientSecret), t)).accepted();
		assertThat(tokensClient.getAccessToken(new ClientCredentials(client.getClientId(), newClientSecret))).accepted().withResponseCode(200);
	}

	@Test
	public void aUserShouldNotBeAbleToUpdateAClientSecretIfTheCurrentClientSecretIsNotProvided() {
		String accountPassword = "alsdifjls3424AA!";
		String currentClientSecret = "sadfljasdf8978wrsls;ASFSADF$$";
		String newClientSecret = "a4423SAFLSDFJSssdflsidfj5234$$";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:user").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withClientSecret(currentClientSecret).withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation(newClientSecret, ""), t)).rejected().withError(422, "PWD-1000", "The current client secret is required.");
	}

	@Test
	public void aUserShouldNotBeAbleToUpdateAClientSecretIfTheCurrentClientSecretDoesNotMatchTheExistingClientSecret() {
		String accountPassword = "alsdifjls3424AA!";
		String currentClientSecret = "sadfljasdf8978wrsls;ASFSADF$$";
		String newClientSecret = "a4423SAFLSDFJSssdflsidfj5234$$";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:user").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withClientSecret(currentClientSecret).withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation(newClientSecret, "currentClientSecret"), t)).rejected().withError(422, "PWD-1001", "The client secret provided does not match your current client secret. Please try again.");
	}

	@Test
	public void aUserShouldNotBeAbleToUpdateAClientSecretIfTheNewClientSecretIsInvalid() {
		String accountPassword = "alsdifjls3424AA!";
		String currentClientSecret = "sadfljasdf8978wrsls;ASFSADF$$";

		AccountRepresentation account = account().withPassword(accountPassword).withRoles("tokamak:user").thatIs().persistent(token).build();
		String t = token().withClient(TEST_CLIENT_CREDENTIALS).withUser(account.getUsername(), accountPassword).thatIs().persistent().build().getAccessToken();

		ScopeRepresentation scope = scope().thatIs().persistent(token).build();
		ClientRepresentation client = client().withClientSecret(currentClientSecret).withScopes(scope).withToken(token).withName("name").withGrantTypes("client_credentials", "refresh_token").thatIs().persistent().build();

		assertThat(clientsClient.updateSecret(client, new SecretsRepresentation("2aaaaA$", currentClientSecret), t)).rejected().withError(422, "PWD-0004", "The password must be at least 12 characters.");
	}
	
	@Test
	public void shouldBeAbleToDeleteAClient() {
		ClientRepresentation client = client().withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();

		Result<ClientRepresentation> result = clientsClient.delete(client.getId(), token.getAccessToken());
		assertThat(result).accepted().withResponseCode(204);

		assertThat(clientsClient.findById(client.getId(), token.getAccessToken())).rejected().withResponseCode(404);
	}

	@Test
	public void shouldBeAbleToFindAnClientById() {
		ClientRepresentation persistent = client().withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();
		Result<ClientRepresentation> response = clientsClient.findById(persistent.getId(), token.getAccessToken());

		assertThat(response).accepted().withResponseCode(200);
		assertThat(response.getInstance().getId()).isEqualTo(persistent.getId());
	}

	@Test
	public void shouldReturnA404WhenAnClientWithTheSpecifiedIdCannotBeFound() {
		Result<ClientRepresentation> response = clientsClient.findById("abcdefg", token.getAccessToken());
		assertThat(response).rejected().withResponseCode(404).withMessage("No such client id: abcdefg");
	}

	@Test
	public void shouldBeAbleToListClients() {
		IntStream.range(1, 5).forEach(i -> client().withGrantTypes("password", "refresh_token").withToken(token).thatIs().persistent().build());

		Result<PaginatedListRepresentation<ClientRepresentation>> result = clientsClient.list(criteria(), token.getAccessToken());
		assertThat(result).accepted().withResponseCode(200);
		assertThat(result.getInstance().getPayload().size()).isGreaterThanOrEqualTo(5);
		assertThat(result.getInstance().getCriteria().getPage()).isNotNull();
		assertThat(result.getInstance().getCriteria().getLimit()).isNotNull();

		assertThat(clientsClient.list(criteria().limit(1).page(4), token.getAccessToken()).getInstance().getPayload()).hasSize(1);
	}

}
