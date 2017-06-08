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

package fm.pattern.tokamak.server.service;

import java.util.List;

import fm.pattern.tokamak.server.model.Audience;
import fm.pattern.valex.Result;
import fm.pattern.valex.annotations.Create;
import fm.pattern.valex.annotations.Delete;
import fm.pattern.valex.annotations.Update;

public interface AudienceService {

	Result<Audience> create(@Create Audience audience);

	Result<Audience> update(@Update Audience audience);

	Result<Audience> delete(@Delete Audience audience);

	Result<Audience> findById(String id);

	Result<Audience> findByName(String name);

	Result<List<Audience>> findExistingById(List<String> ids);

	Result<List<Audience>> list();

}
