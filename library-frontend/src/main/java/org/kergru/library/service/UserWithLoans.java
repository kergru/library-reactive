package org.kergru.library.service;

import java.util.List;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;

public record UserWithLoans(UserDto user, List<LoanDto> loans) { }
