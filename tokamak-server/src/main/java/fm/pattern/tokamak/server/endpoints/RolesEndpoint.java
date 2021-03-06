/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fm.pattern.tokamak.server.endpoints;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fm.pattern.tokamak.authorization.Authorize;
import fm.pattern.tokamak.sdk.model.RoleRepresentation;
import fm.pattern.tokamak.sdk.model.RolesRepresentation;
import fm.pattern.tokamak.server.conversion.RoleConversionService;
import fm.pattern.tokamak.server.model.Role;
import fm.pattern.tokamak.server.service.RoleService;

@RestController
public class RolesEndpoint extends Endpoint {

	private final RoleService roleService;
	private final RoleConversionService roleConversionService;

	@Autowired
	public RolesEndpoint(RoleService roleService, RoleConversionService roleConversionService) {
		this.roleService = roleService;
		this.roleConversionService = roleConversionService;
	}

	@Authorize(scopes = "roles:create")
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/v1/roles", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public RoleRepresentation create(@RequestBody RoleRepresentation representation) {
		Role role = roleConversionService.convert(representation);
		Role created = roleService.create(role).orThrow();
		return roleConversionService.convert(roleService.findById(created.getId()).orThrow());
	}

	@Authorize(scopes = "roles:update")
	@RequestMapping(value = "/v1/roles/{id}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public RoleRepresentation update(@PathVariable String id, @RequestBody RoleRepresentation representation) {
		Role role = roleConversionService.convert(representation, roleService.findById(id).orThrow());
		return roleConversionService.convert(roleService.update(role).orThrow());
	}

	@Authorize(scopes = "roles:delete")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/v1/roles/{id}", method = DELETE)
	public void delete(@PathVariable String id) {
		Role role = roleService.findById(id).orThrow();
		roleService.delete(role).orThrow();
	}

	@Authorize(scopes = "roles:read")
	@RequestMapping(value = "/v1/roles/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
	public RoleRepresentation findById(@PathVariable String id) {
		Role role = roleService.findById(id).orThrow();
		return roleConversionService.convert(role);
	}

	@Authorize(scopes = "roles:read")
	@RequestMapping(value = "/v1/roles/name/{name}", method = GET, produces = APPLICATION_JSON_VALUE)
	public RoleRepresentation findByName(@PathVariable String name) {
		Role role = roleService.findByName(name).orThrow();
		return roleConversionService.convert(role);
	}

	@Authorize(scopes = "roles:read")
	@RequestMapping(value = "/v1/roles", method = GET, produces = APPLICATION_JSON_VALUE)
	public RolesRepresentation list() {
		List<Role> roles = roleService.list().orThrow();
		return new RolesRepresentation(roles.stream().map(role -> roleConversionService.convert(role)).collect(Collectors.toList()));
	}

}
