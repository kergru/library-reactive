package org.kergru.library.model;

import java.time.Instant;

public record LoanStatusDto(
    Boolean available,
    Long borrowedBy,
    Instant borrowedAt
) {}
