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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fm.pattern.tokamak.server.model.Account;
import fm.pattern.tokamak.server.security.AuthenticatedAccount;
import fm.pattern.tokamak.server.security.CurrentAuthenticatedAccountContext;
import fm.pattern.tokamak.server.security.CurrentAuthenticatedClientContext;

@Service("authenticationService")
class AccountAuthenticationServiceImpl implements AccountAuthenticationService {

	private final AccountService accountService;

	@Autowired
	AccountAuthenticationServiceImpl(AccountService accountService) {
		this.accountService = accountService;
	}

	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		if (CurrentAuthenticatedAccountContext.hasAuthenticatedAccount()) {
			AuthenticatedAccount account = CurrentAuthenticatedAccountContext.getAuthenticatedAccount();
			if (account.getUsername().equals(username)) {
				return CurrentAuthenticatedAccountContext.getAuthenticatedAccount();
			}
			CurrentAuthenticatedAccountContext.clear();
		}

		Account account = accountService.findByUsername(username).getInstance();
		if (account == null || account.isLocked()) {
			CurrentAuthenticatedClientContext.clear();
			throw new UsernameNotFoundException("Could not find an active account with email address: " + username);
		}

		return CurrentAuthenticatedAccountContext.setAuthenticatedAccount(new AuthenticatedAccount(account));
	}

}
