package fm.pattern.jwt.server.config;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jdbc")
public class DatabaseProperties {

	@NotEmpty(message = "The database url cannot be empty")
	private String url;

	@NotEmpty(message = "The database username cannot be empty")
	private String username;

	@NotEmpty(message = "The database password cannot be empty")
	private String password;

	@NotEmpty(message = "The database driver class cannot be empty")
	private String driver;

	@NotEmpty(message = "The database dialect cannot be empty")
	private String dialect;

	@NotEmpty(message = "The database schema name cannot be empty")
	private String schema;

	@NotEmpty(message = "The migration resources cannot be empty")
	private String migrations;

	private boolean migrate = true;

	private boolean encrypted;

	private String ddl;

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getDdl() {
		return ddl;
	}

	public void setDdl(String ddl) {
		this.ddl = ddl;
	}

	public String getMigrations() {
		return migrations;
	}

	public void setMigrations(String migrations) {
		this.migrations = migrations;
	}

	public boolean shouldMigrate() {
		return migrate;
	}

	public void setMigrate(boolean migrate) {
		this.migrate = migrate;
	}

}
